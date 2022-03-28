<?php

namespace App\Http\Controllers;

use App\CsvItem;
use App\PolyItem;
use App\Traits\Adding;
use App\Traits\Common;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use OsmPbf\Reader;

class CityController extends Controller
{
    use Common, Adding;

    private function IsCityDuplicate($name): bool
    {
        return DB::table('city')->where('name', $name)->first() != null;
    }

    private function IsCityAdded($id): bool
    {
        return DB::table('city')->where('id', $id)->first() != null;
    }

    public function ParseCities(Request $request): \Illuminate\Http\JsonResponse
    {
        $this->ParseCitiesFromMap($request["map_id"]);

        DB::table("users")->update(array(
            "city_download" => 1
        ));

        $this->UpdateUsersCityDownload();

        return $this->doneResponse();
    }

    public function EditCity(Request $request)
    {
        if(!$this->IsCityAdded($request["id"]))
        {
            return $this->doneResponse();
        }

        $city = DB::table("city")->where("id", $request["id"])->first();

        if($city->name != $request["name"]) {
            if($this->IsCityDuplicate($request["name"]))
            {
                return $this->doneResponse();
            }

            DB::table("city")->where("id", $request["id"])->
                update(array(
                    "name" => $request["name"]
                ));
        }

        $this->UpdateUsersCityDownload();

        return $this->doneResponse();
    }

    public function RemoveCity(Request $request)
    {
        if(!$this->IsCityAdded($request["id"]))
        {
            return $this->doneResponse();
        }

        DB::table("city")->where("id", $request["id"])->delete();

        $count1 = DB::table("list")->where("city_id", $request["id"])->update(array("city_id" => -1));
        $count2 = DB::table("csv")->where("city_id", $request["id"])->update(array("city_id" => -1));
        $count3 = DB::table("load")->where("city_id", $request["id"])->update(array("city_id" => -1));
        $count4 = DB::table("poly")->where("city_id", $request["id"])->update(array("city_id" => -1));
        if($count1 > 0 || $count2 > 0 || $count3 > 0 || $count4 > 0) {
            DB::table("users")->update(array('force_download' => 1));
        } else {
            $this->UpdateUsersCityDownload();
        }

        return $this->doneResponse();
    }

    private function UpdateUsersCityDownload()
    {
        DB::table("users")->where("role_id", 1)
            ->update(array(
                "city_download" => 1
            ));
    }
}
