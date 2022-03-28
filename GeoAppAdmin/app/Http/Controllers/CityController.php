<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Routing\Controller;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Redirect;
use App\Http\Traits\Common;
use Illuminate\Support\Facades\Session;
use Illuminate\Support\Facades\Storage;

class CityController extends Controller
{
    use Common;

    public function ShowCityList() {
        $list = DB::table("city")
            ->orderBy('name', 'ASC')
            ->get();
        $maps = DB::table("maps")->get();

        foreach($list as $row) {
            if($row->map_id != -1) {
                $row->map_id = DB::table("maps")->where("id", $row->map_id)->value("name");
            }
        }

        Session::put("current", "city");
        return view("city_list", compact("list", "maps"));
    }

    public function ShowEditCity($id) {
        $city = DB::table("city")->where("id", $id)->first();
        return view("city_edit", compact("city"));
    }

    public function DeleteCity($id) {
        $params = array("id" => $id);
        $response = $this->MakeRequest($this->APIPath()."removeCity", $params, true);
        return Redirect::to("/citylist");
    }

    public function ParseCities(Request $request) {
        set_time_limit(500);
        $params = array(
            "map_id" => $request["map_id"]
        );
        $this->MakeRequest($this->APIPath()."parseCities", $params, true);

        return Redirect::to('/citylist');
    }

    public function EditCity(Request $request) {
        $params = array(
            "id" => $request["id"],
            "name" => $request["name"]
        );
        $this->MakeRequest($this->APIPath()."editCity", $params, true);

        return Redirect::to('/citylist');
    }
}
