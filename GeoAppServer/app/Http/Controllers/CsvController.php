<?php

namespace App\Http\Controllers;

use App\CsvItem;
use App\Traits\Adding;
use App\Traits\Common;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class CsvController extends Controller
{
    use Common, Adding;

    private function IsCSVDuplicate($name, $city_id): bool
    {
        return DB::table('csv')
                ->where('name', $name)
                ->where('city_id', $city_id)
                ->first() != null;
    }

    private function IsCSVAdded($id): bool
    {
        return DB::table('csv')->where('id', $id)->first() != null;
    }

    private function IsAttributeAdded($id): bool
    {
        return DB::table('attribute')->where('id', $id)->first() != null;
    }

    public function InsertCSV(Request $request): \Illuminate\Http\JsonResponse
    {
        $name = $request["name"];
        $load_csv = $request["load"];

        $notfound = array();
        if($this->IsCSVDuplicate($name, $request["city"]))
        {
            $response["notfound"] = $notfound;
            return response()->json($response);
        }

        $data = $this->Decompress($request["data"]);
        $this->SaveFile(storage_path()."/app/csv/".$request["name"].".csv", $data, false);

        $item = new CsvItem();
        $item->name = $name;
        $item->loaded = 0;
        $item->legend_id = $request["legend"];
        $item->city_id = $request["city"];
        $item->created = $this->getTimeInMillis();
        $item->save();

        if($load_csv == 1)
        {
            $notfound = $this->LoadDataFromCSV($item->id);
        }

        $this->UpdateUsersAttributeDownload();

        $response["notfound"] = $notfound;
        return response()->json($response);
    }

    public function EditCSV(Request $request): \Illuminate\Http\JsonResponse
    {
        $notfound = array();
        if(!$this->IsCSVAdded($request["id"]))
        {
            $response["notfound"] = $notfound;
            return response()->json($response);
        }

        $csv = DB::table("csv")->where("id", $request["id"])->first();

        if($csv->name != $request["name"]) {
            if($this->IsCSVDuplicate($request["name"], $request["city"]))
            {
                $response["notfound"] = $notfound;
                return response()->json($response);
            }

            $this->RenameFile(
                storage_path()."/app/csv/".$csv->name.".csv",
                storage_path()."/app/csv/".$request["name"].".csv"
            );
        }

        DB::table("csv")->where("id", $request["id"])
            ->update(array(
                "name" => $request["name"],
                "legend_id" => $request["legend"],
            ));

        if($request["load"] == 1 && $csv->loaded == 0)
        {
            $notfound = $this->LoadDataFromCSV($csv->id);
        }
        else if($request["load"] == 0 && $csv->loaded == 1)
        {
            $this->RemoveDataFromCSV($csv->id);
        }

        if($csv->city_id != $request["city_id"])
        {
            if(DB::table("csv")->where("id", $request["id"])->value("loaded") == 1) {
                $this->RemoveDataFromCSV($csv->id);
                DB::table("csv")->where("id", $request["id"])->
                    update(array(
                        "city_id" => $request["city_id"]
                    ));
                $notfound = $this->LoadDataFromCSV($csv->id);
            } else {
                DB::table("csv")->where("id", $request["id"])->
                    update(array(
                        "city_id" => $request["city_id"]
                    ));
            }
        }

        $this->UpdateUsersAttributeDownload();

        $response["notfound"] = $notfound;
        return response()->json($response);
    }

    public function RemoveCSV(Request $request): \Illuminate\Http\JsonResponse
    {
        if(!$this->IsCSVAdded($request["id"]))
        {
            return $this->doneResponse();
        }

        $csv = DB::table("csv")->where("id", $request["id"])->first();

        $this->RemoveFile(storage_path()."/app/csv/".$csv->name.".csv");
        DB::table("attribute")->where("csv_id", $csv->id)->delete();
        DB::table("csv")->where("id", $request["id"])->delete();

        $this->UpdateUsersAttributeDownload();

        return $this->doneResponse();
    }

    private function RemoveDataFromCSV($id)
    {
        $csv = DB::table("csv")->where("id", $id)->first();

        DB::table('attribute')->where("csv_id", $csv->id)->delete();
        DB::table("csv")->where("id", $csv->id)
            ->update(array(
                "loaded" => 0
            ));
    }

    public function EditAttribute(Request $request): \Illuminate\Http\JsonResponse
    {
        if(!$this->IsAttributeAdded($request["id"]))
        {
            return $this->doneResponse();
        }

        $attribute = DB::table("attribute")->where("id", $request["id"])->first();
        $list_item = DB::table("list")->where("id", $attribute->list_id)->first();
        if($list_item->type == "LineString") {
            if($list_item->city_id != -1) {
                $list = DB::table("list")->where("city_id", $list_item->city_id)
                    ->where("name", $list_item->name)
                    ->pluck('id')->toArray();
            } else {
                $list = DB::table("list")->where("loadjson_id", $list_item->loadjson_id)
                    ->where("name", $list_item->name)
                    ->pluck('id')->toArray();
            }

            DB::table("attribute")->whereIn("list_id", $list)
                ->update(array(
                    "csv_id" => $request["csv_id"],
                    "value" => $request["value"]
                ));
        }
        else {
            DB::table("attribute")->where("id", $request["id"])
                ->update(array(
                    "csv_id" => $request["csv_id"],
                    "value" => $request["value"]
                ));
        }

        $this->UpdateUsersAttributeDownload();

        return $this->doneResponse();
    }

    public function DeleteAttribute(Request $request): \Illuminate\Http\JsonResponse
    {
        if(!$this->IsAttributeAdded($request["id"]))
        {
            return $this->doneResponse();
        }

        $attribute = DB::table("attribute")->where("id", $request["id"])->first();

        $list_item = DB::table("list")->where("id", $attribute->list_id)->first();
        if($list_item->type == "LineString") {
            if($list_item->city_id != -1) {
                $list = DB::table("list")->where("city_id", $list_item->city_id)
                    ->where("name", $list_item->name)
                    ->pluck('id')->toArray();
            } else {
                $list = DB::table("list")->where("loadjson_id", $list_item->loadjson_id)
                    ->where("name", $list_item->name)
                    ->pluck('id')->toArray();
            }

            DB::table("attribute")->whereIn("list_id", $list)->delete();
        } else {
            DB::table("attribute")->where("id", $request["id"])->delete();
        }

        $this->UpdateUsersAttributeDownload();

        return $this->doneResponse();
    }

    public static function UpdateUsersAttributeDownload()
    {
        DB::table("users")->where("role_id", 1)
            ->update(array(
                "attribute_download" => 1
            ));
    }
}
