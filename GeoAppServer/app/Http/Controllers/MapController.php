<?php

namespace App\Http\Controllers;

use App\LegendItem;
use App\MapItem;
use App\Traits\Common;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class MapController extends Controller
{
    use Common;

    public static $MapLegendDefault = "defaultLegend";

    private function IsMapDuplicate($name): bool
    {
        return DB::table('maps')->where('name', $name)->first() != null;
    }

    private function IsMapAdded($id): bool
    {
        return DB::table('maps')->where('id', $id)->first() != null;
    }

    private function IsMapLegendDuplicate($name): bool
    {
        return DB::table('legend')->where('name', $name)->first() != null;
    }

    private function IsMapLegendAdded($id): bool
    {
        return DB::table('legend')->where('id', $id)->first() != null;
    }

    public function InsertMap(Request $request)
    {
        if($this->IsMapDuplicate($request["name"]))
        {
            return $this->doneResponse();
        }

        $item = new MapItem();
        $item->name = $request["name"];
        $item->type = $request["type"];
        $item->default = 0;
        $item->created = $this->getTimeInMillis();
        $item->save();

        return $this->doneResponse();
    }

    public function EditMap(Request $request)
    {
        if(!$this->IsMapAdded($request["id"]))
        {
            return $this->doneResponse();
        }

        $map = DB::table("maps")->where("id", $request["id"])->first();

        if($map->name != $request["name"]) {
            if($this->IsMapDuplicate($request["name"]))
            {
                return $this->doneResponse();
            }

            $this->RenameFile(
                storage_path() . "/app/maps/" . $map->name . "." . $map->type,
                storage_path() . "/app/maps/" . $request["name"] . "." . $map->type
            );

            DB::table("maps")->where("id", $request["id"])->
                update(array(
                    "name" => $request["name"]
                ));
        }

        return $this->doneResponse();
    }

    public function RemoveMap(Request $request)
    {
        if(!$this->IsMapAdded($request["id"]))
        {
            return $this->doneResponse();
        }

        $map = DB::table("maps")->where("id", $request["id"])->first();

        $this->RemoveFile(storage_path()."/app/maps/".$map->name.".".$map->type);

        if($request["removeFromDB"] == 1) {
            DB::table("maps")->where("id", $request["id"])->delete();
        }

        return $this->doneResponse();
    }

    public function InsertMapLegend(Request $request)
    {
        if($this->IsMapLegendDuplicate($request["name"]))
        {
            return $this->doneResponse();
        }

        $colors = $request["colors"];

        $item = new LegendItem();
        $item->name = $request["name"];
        $item->zero = $colors[0];
        $item->first = $colors[1];
        $item->second = $colors[2];
        $item->third = $colors[3];
        $item->fourth = $colors[4];
        $item->fifth = $colors[5];
        $item->sixth = $colors[6];
        $item->seventh = $colors[7];
        $item->eight = $colors[8];
        $item->ninth = $colors[9];
        $item->default = 0;
        $item->created = $this->getTimeInMillis();
        $item->save();

        $this->UpdateUsersLegendDownload();
        return $this->doneResponse();
    }

    public function EditMapLegend(Request $request)
    {
        if(!$this->IsMapLegendAdded($request["id"]))
        {
            return $this->doneResponse();
        }

        $legend = DB::table("legend")->where("id", $request["id"])->first();
        if($legend->name != $request["name"]) {
            if($this->IsMapLegendDuplicate($request["name"]))
            {
                return $this->doneResponse();
            }
        }

        $colors = $request["colors"];

        DB::table("legend")->where("id", $request["id"])->
            update(array(
                "name" => $request["name"],
                "zero" => $colors[0],
                "first" => $colors[1],
                "second" => $colors[2],
                "third" => $colors[3],
                "fourth" => $colors[4],
                "fifth" => $colors[5],
                "sixth" => $colors[6],
                "seventh" => $colors[7],
                "eight" => $colors[8],
                "ninth" => $colors[9],
        ));

        $this->UpdateUsersLegendDownload();
        return $this->doneResponse();
    }

    public function RemoveMapLegend(Request $request)
    {
        if(!$this->IsMapLegendAdded($request["id"]))
        {
            return $this->doneResponse();
        }

        $defaultLegendId = DB::table("legend")->where("name", self::$MapLegendDefault)->value("id");
        $count = DB::table("csv")->where("legend_id", $request["id"])->count();
        if($count > 0)
        {
            DB::table("csv")->where("legend_id", $request["id"])
                ->update(array(
                    "legend_id" => $defaultLegendId
                ));

            CsvController::UpdateUsersAttributeDownload();
        }

        DB::table("legend")->where("id", $request["id"])->delete();
        $this->UpdateUsersLegendDownload();

        return $this->doneResponse();
    }

    private function UpdateUsersLegendDownload()
    {
        DB::table("users")->where("role_id", 1)
            ->update(array(
                "legend_download" => 1
            ));
    }
}
