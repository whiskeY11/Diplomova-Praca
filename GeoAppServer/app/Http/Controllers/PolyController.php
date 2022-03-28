<?php

namespace App\Http\Controllers;

use App\PolyItem;
use App\Traits\Adding;
use App\Traits\Common;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use OsmPbf\Reader;

class PolyController extends Controller
{
    use Common, Adding;

    private function IsPolyDuplicate($name): bool
    {
        return DB::table('poly')->where('name', $name)->first() != null;
    }

    private function IsPolyAdded($id): bool
    {
        return DB::table('poly')->where('id', $id)->first() != null;
    }

    public function InsertPoly(Request $request)
    {
        $name = $request["name"];
        $map_id = $request["map_id"];
        $city_id = $request["city_id"];
        $create_json = $request["create_json"];
        $load_json = $request["load_json"];

        if($this->IsPolyDuplicate($name)) {
            echo "Poly is already added!\n";
            return $this->doneResponse();
        }

        $data = $this->Decompress($request["data"]);

        $this->SaveFile(storage_path()."/app/poly/".$name.".poly", $data, false);

        $item = new PolyItem();
        $item->name = $name;
        $item->map_id = $map_id;
        $item->city_id = $city_id;
        $item->json_created = $create_json;
        $item->created = $this->getTimeInMillis();
        $item->save();

        if($create_json == 1)
        {
            $this->CreateJSONFromPBF($item->id, $load_json == 1);
        }

        return $this->doneResponse();
    }

    public function EditPoly(Request $request): \Illuminate\Http\JsonResponse
    {
        $id = $request["id"];
        $load_json = $request["load_json"];

        if(!$this->IsPolyAdded($id))
        {
            return $this->doneResponse();
        }

        $poly = DB::table("poly")->where("id", $id)->first();

        if($poly->name != $request["name"])
        {
            if($this->IsPolyDuplicate($request["name"])) {
                return $this->doneResponse();
            }

            if($poly->json_created == 1) {
                $this->RenameFile(
                    storage_path() . "/app/pbf/" . $poly->name . ".pbf",
                    storage_path() . "/app/pbf/" . $request["name"] . ".pbf"
                );
            }

            $this->RenameFile(
                storage_path() . "/app/poly/" . $poly->name . ".poly",
                storage_path() . "/app/poly/" . $request["name"] . ".poly"
            );

            DB::table("poly")->where("id", $id)->
                update(array(
                    "name" => $request["name"]
                ));
        }

        if($poly->city_id != $request["city_id"])
        {
            $loadId = DB::table("load")->where("poly_id", $id)->value("id");
            DB::table("list")->where("loadjson_id", $loadId)->
                update(array(
                    "city_id" => $request["city_id"],
                    "updated" => $this->getTimeInMillis()
                ));

            DB::table("csv")->where("city_id", $poly->city_id)->
                update(array(
                    "city_id" => $request["city_id"]
                ));

            DB::table("load")->where("poly_id", $id)->
                update(array(
                    "city_id" => $request["city_id"]
            ));

            DB::table("poly")->where("id", $id)->
                update(array(
                    "city_id" => $request["city_id"]
                ));
        }

        if($request["create_json"] == 1)
        {
            if($poly->json_created == 0) {
                $this->CreateJSONFromPBF($poly->id, $load_json == 1);
            } else if($load_json == 1) {
                $json = DB::table("load")->where("poly_id", $id)->first();
                $this->AddPBFJSONData($json->id);
            } else if($load_json == 0) {
                $json = DB::table("load")->where("poly_id", $id)->first();
                $this->removeGeoJSONData($json->id);
            }
        }
        else if($request["create_json"] == 0 && $poly->json_created == 1)
        {
            $this->RemoveFile(storage_path()."/app/pbf/".$request["name"].".pbf");
            $this->RemovePBFJSON($poly->id);
        }

        $response["result"] = "done";
        return response()->json($response);
    }

    public function RemovePoly(Request $request)
    {
        $id = $request["id"];

        if(!$this->IsPolyAdded($id))
        {
            return $this->doneResponse();
        }

        $poly = DB::table("poly")->where("id", $id)->first();

        if($poly->json_created == 1)
        {
            $this->RemoveFile(storage_path()."/app/pbf/".$poly->name.".pbf");
            $this->RemovePBFJSON($poly->id);
        }

        $this->RemoveFile(storage_path()."/app/poly/".$poly->name.".poly");

        DB::table("poly")->where("id", $id)->delete();

        return $this->doneResponse();
    }

    private function CreateJSONFromPBF($id, $load): \Illuminate\Http\JsonResponse
    {
        if(!$this->IsPolyAdded($id)) {
            return $this->doneResponse();
        }

        $poly = DB::table("poly")->where("id", $id)->first();
        $name = $poly->name;
        $map = DB::table("maps")->where("id", $poly->map_id)->first();

        $path = storage_path()."/app/";
        exec('cd '.$path.'&&'.' osmconvert maps/'.$map->name.'.'.$map->type.' -B=poly/'.$name.'.poly -o=pbf/'.$name.'.pbf');

        $file_handler = fopen(storage_path()."/app/pbf/".$name.".pbf", "rb");
        $pbfreader = new Reader($file_handler);

        ini_set('memory_limit', '-1');
        $pbfreader->skipToBlock(0);
        while ($pbfreader->next()) {
            $elements = $pbfreader->getElements();
            if($elements != null) {
                switch($elements["type"]) {
                    case "node":
                        $this->ProcessNodes($elements, $name);
                        break;
                    case "way":
                        $this->ProcessWays($elements, $name);
                        break;
                }
            }
        }

        $idload = $this->addLoadItem($id, 'ways_'.$name, "json",
            DB::table("icons")->where("name", "defaultIcon")->value("id"), $poly->city_id);
        if($load) {
            $this->AddPBFJSONData($idload);
        }
        DB::table("poly")->where("id", $id)->update(array("json_created" => 1));

        return $this->doneResponse();
    }

    private function RemovePBFJSON($id)
    {
        $json = DB::table("load")->where("poly_id", $id)->first();
        if($json != null)
        {
            $this->RemoveFile(storage_path() . "/app/files/" . $json->name . "." . $json->type);

            if ($json->loaded == 1) {
                DB::table('list')->where('loadjson_id', $json->id)->update(['deleted' => 1, 'updated' => $this->getTimeInMillis()]);
            }

            DB::table("load")->where("poly_id", $id)->delete();
            DB::table("poly")->where("id", $id)->update(array("json_created" => 0));
        }
    }

    private function ProcessNodes($elements, $name)
    {
        $nodes = [];

        foreach ($elements['data'] as $element) {
            $insert_latlon = [
                "latitude" => $element["latitude"],
                "longitude" => $element["longitude"]
            ];
            $nodes[$element['id']] = $insert_latlon;
        }

        $this->SaveFile(storage_path() . '/app/files/nodes_'.$name.'.json', $nodes);
    }

    private function ProcessWays($elements, $name)
    {
        $nodes = $this->ReadFile(storage_path() . '/app/files/nodes_'.$name.'.json');

        $waysParsed = array();
        foreach ($elements['data'] as $element) {
            if(isset($element["tags"]["highway"]) && isset($element["tags"]["name"])) {
                $nodesParsed = array();
                foreach ($element["nodes"] as $node) {
                    if (isset($nodes[$node["id"]])) {
                        $record = $nodes[$node["id"]];
                        $insert_node = [
                            "lat" => $record["latitude"],
                            "lng" => $record["longitude"]
                        ];
                        $nodesParsed[] = $insert_node;
                    }
                }

                $insert_element = [
                    "name" => $element["tags"]["name"]["value"],
                    "nodes" => $nodesParsed
                ];
                $waysParsed[] = $insert_element;
            }
        }

        $this->SaveFile(storage_path() . '/app/files/ways_'.$name.'.json', $waysParsed);
        $this->RemoveFile(storage_path() . '/app/files/nodes_'.$name.'.json');
    }
}
