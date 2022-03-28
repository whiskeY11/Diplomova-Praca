<?php

namespace App\Http\Controllers;

use App\Traits\Adding;
use App\Traits\Common;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class DBController extends Controller
{
    use Common, Adding;

    public function addFile(Request $request): \Illuminate\Http\JsonResponse
    {
        $name = $request["name"];
        $iconID = $request["iconID"];
        $type = $request["type"];
        $cityID = $request["cityID"];

        $data = $this->Decompress($request["data"]);
        $this->SaveFile(storage_path()."/app/files/".$name.".".$type, $data, false);

        if($this->isFileDuplicate($name)) {
            echo "File ".$name." is already added!\n";
            return $this->doneResponse();
        }

        echo "Adding ".$name." to the database...\n";

        $item = $this->addLoadItem(-1, $name, $type, $iconID, $cityID);
        if($request["load"] == 1) {
            $this->addGeoJSONData($item);
        }

        return $this->doneResponse();
    }

    public function editFile(Request $request): \Illuminate\Http\JsonResponse
    {
        $file = DB::table("load")->where("id", $request["id"])->first();
        if($file->name != $request["name"]) {
            if($this->isFileDuplicate($request["name"])) {
                echo "File ".$request["name"]." is already added!\n";
                return $this->doneResponse();
            }

            $this->RenameFile(
                storage_path()."/app/files/".$file->name.".".$file->type,
                storage_path()."/app/files/".$request["name"].".".$file->type
            );
        }

        $icon = DB::table("icons")->where("id", $request["iconID"])->value("name");
        if($file->icon != $icon) {
            DB::table("list")->where("loadjson_id", $file->id)
                ->where("type", "Point")
                ->update(array(
                    "icon" => $icon,
                    "updated" => $this->getTimeInMillis()
                ));
        }

        if($file->city_id != $request["city_id"]) {
            DB::table("list")->where("loadjson_id", $file->id)
                ->update(array(
                    "city_id" => $request["city_id"],
                    "updated" => $this->getTimeInMillis()
                ));

            if($file->poly_id != -1) {
                DB::table("csv")->where("city_id", $file->city_id)->
                    update(array(
                        "city_id" => $request["city_id"]
                    ));

                DB::table("poly")->where("id", $file->poly_id)
                    ->update(array(
                        "city_id" => $request["city_id"]
                    ));
            }
        }

        DB::table("load")->where("id", $request["id"])
            ->update(array(
                    "name" => $request["name"],
                    "city_id" => $request["city_id"],
                    "icon" => $icon
                )
            );

        if($request["load"] == 1) {
            if($file->poly_id == -1) {
                $this->addGeoJSONData($request["id"]);
            } else {
                $this->AddPBFJSONData($request["id"]);
            }
        } else {
            $this->removeGeoJSONData($request["id"]);
        }

        return $this->doneResponse();
    }

    public function removeJson(Request $request): \Illuminate\Http\JsonResponse
    {
        $id = $request["id"];
        if(!$this->isFileAdded($id)){
            echo "File with ID: ".$id." is not added to database, ignoring...\n";
            return $this->doneResponse();
        }

        $this->removeGeoJSONData($id);
        $file = DB::table('load')->where('id', $id)->first();

        $this->RemoveFile(storage_path()."/app/files/".$file->name.".".$file->type);

        if($file->poly_id != -1)
        {
            DB::table('poly')->where('id', $file->poly_id)->
                update(array(
                    "json_created" => 0
                ));

            $poly_name = DB::table("poly")->where("id", $file->poly_id)->value("name");
            $this->RemoveFile(storage_path()."/app/pbf/".$poly_name.".pbf");
        }

        DB::table('load')->where('id', $id)->delete();
        return $this->doneResponse();
    }

    private function addDataFromAllJSON() {
        if($this->isLoadEmpty()) {
            echo "No Files added to DB!, ignoring...\n";
            return $this->doneResponse();
        }

        $jsonsToLoad = DB::table("load")->get();

        foreach($jsonsToLoad as $item) {
            if($item->poly_id == -1) {
                $this->addGeoJSONData($item->id);
            } else {
                $this->AddPBFJSONData($item->id);
            }
        }

        return $this->doneResponse();
    }

    private function handleMultipleNames($properties): ?string
    {
        $name = null;
        $array = array("N_OBJ", "TEXT", "name");
        foreach($array as $row) {
            if (isset($properties[$row])) {
                $name = $properties[$row];
                break;
            }
        }

        if($name == null) $name = "Unknown";

        return $name;
    }

    private function addGeoJSONData($id)
    {
        if($this->isLoadEmpty()) {
            echo "No Files added to DB!, ignoring...\n";
            return $this->doneResponse();
        }
        if(!$this->isFileAdded($id)){
            echo "FILE with ID :".$id." is not added to database, ignoring...\n";
            return $this->doneResponse();
        }
        if($this->isFileLoaded($id)) {
            echo "FILE with ID :".$id . " is already loaded, ignoring...\n";
            return $this->doneResponse();
        }

        echo "Loading FILE with ID: ".$id."...\n";

        $loadJson = DB::table('load')->where('id', $id)->first();
        $json = $this->ReadFile(storage_path() . '/app/files/'.$loadJson->name.'.'.$loadJson->type);

        $userData = $json['features'];
        foreach($userData as $row) {
            $properties = $row['properties'];
            $geometry = $row['geometry'];

            $name = $this->handleMultipleNames($properties);
            $type = $geometry["type"];
            $corArray = $geometry["coordinates"];
            $id = $this->addItem($loadJson->id, $name, $type, $loadJson->icon, -1, $loadJson->city_id);

            switch($type) {
                case "Point":
                    $lng = $corArray[0];
                    $lat = $corArray[1];
                    $this->addCords($loadJson->id, $id, $lng, $lat);
                    break;
                case "LineString":
                    $data = array();
                    foreach ($corArray as $cordRow) {
                        $data[] =[
                            'loadjson_id' => $loadJson->id,
                            'idlist' => $id,
                            'lng' => $cordRow[0],
                            'lat' => $cordRow[1]
                        ];
                    }
                    $this->addAllCords($data);
                    break;
                case "Polygon":
                    foreach($corArray as $row) {
                        $data = array();
                        foreach ($row as $cordRow) {
                            $data[] =[
                                'loadjson_id' => $loadJson->id,
                                'idlist' => $id,
                                'lng' => $cordRow[0],
                                'lat' => $cordRow[1]
                            ];
                        }
                        $this->addAllCords($data);
                        break;
                    }
                    break;
            }
        }

        DB::table("load")->where("id", $loadJson->id)->update(['loaded' => 1]);

        return $this->doneResponse();
    }

    private function LoadAttributesFromAllCSV() {
        $csvs = DB::table("csv")->get();

        foreach($csvs as $csv) {
            $this->LoadDataFromCSV($csv->id);
        }
    }

    public function reimportAll(): \Illuminate\Http\JsonResponse
    {
        $response["result"] = null;

        try {
            DB::table("list")->delete();
            DB::table("cords")->delete();
            DB::table("attribute")->delete();
            DB::table('load')->where('loaded', '=', 1)->update(array('loaded' => 0));
            DB::table("users")->update(array('force_download' => 1));
            $this->addDataFromAllJSON();
            $this->LoadAttributesFromAllCSV();
        } catch (\Exception $e) {
            $response["result"] = $e->getMessage();
            //throw new HttpException(500, $e->getMessage());
        }
        return response()->json($response);
    }

    public function exportBounds(): \Illuminate\Http\JsonResponse
    {
        echo "Exporting bounds... \n";

        $bounds = DB::table("bounds")->get();

        $geojson = array(
            'type'      => 'FeatureCollection',
            'features'  => array()
        );

        foreach($bounds as $row) {
            $feature = array(
                'type' => 'Feature',
                'geometry' => array(
                    'type' => "Polygon",
                    'coordinates' => array(
                        array(
                            array($row->lng_west, $row->lat_north),
                            array($row->lng_west, $row->lat_south),
                            array($row->lng_east, $row->lat_south),
                            array($row->lng_east, $row->lat_north),
                            array($row->lng_west, $row->lat_north),
                        )
                    )
                ),
                'properties' => array()
            );

            $geojson['features'][] = $feature;
        }

        $export_fileName = "bounds_export.json";
        $path = storage_path() . '/app/'.$export_fileName;
        $this->SaveFile($path, $geojson);

        return $this->doneResponse();
    }
}
