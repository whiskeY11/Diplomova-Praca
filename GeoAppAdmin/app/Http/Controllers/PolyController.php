<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Routing\Controller;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Redirect;
use App\Http\Traits\Common;
use Illuminate\Support\Facades\Session;

class PolyController extends Controller
{
    use Common;

    public function ShowPolyList() {
        $list = DB::table("poly")->get();
        $maps = DB::table("maps")->get();
        $cities = DB::table("city")
            ->orderBy('name', 'ASC')
            ->get();

        foreach($list as $row) {
            $row->map_id = DB::table("maps")->where("id", $row->map_id)->value("name");
            if($row->city_id != -1) {
                $row->city_id = DB::table("city")->where("id", $row->city_id)->value("name");
            }
        }

        Session::put("current", "poly");
        return view("poly_list", compact("list", "maps", "cities"));
    }

    public function ShowEditPoly($id) {
        $poly = DB::table("poly")->where("id", $id)->first();
        $cities = DB::table("city")
            ->orderBy('name', 'ASC')
            ->get();
        $json_loaded = DB::table("load")->where("poly_id", $id)->value("loaded");
        return view("poly_edit", compact("poly", "json_loaded", "cities"));
    }

    public function DeletePoly($id) {
        $params = array("id" => $id);
        $response = $this->MakeRequest($this->APIPath()."removePoly", $params, true);
        return Redirect::to("/polylist");
    }

    public function UploadPoly(Request $request) {
        set_time_limit(500);
        $file = $request->file('file');
        $file_name = $file->getClientOriginalName();
        $data = $this->Compress(file_get_contents($file));

        $params = array(
            "name" => strtok($file_name, "."),
            "data" => $data,
            "map_id" => $request["map_id"],
            "city_id" => $request["city"],
            "create_json" => $request->has("create_json") ? 1 : 0,
            "load_json" => $request->has("load_json") ? 1 : 0
        );
        $this->MakeRequest($this->APIPath()."addPoly", $params, true);

        return Redirect::to('/polylist');
    }

    public function EditPoly(Request $request) {
        set_time_limit(500);
        $params = array(
            "id" => $request["id"],
            "name" => $request["name"],
            "city_id" => $request["city"],
            "create_json" => $request->has("create_json") ? 1 : 0,
            "load_json" => $request->has("load_json") ? 1 : 0
        );
        $this->MakeRequest($this->APIPath()."editPoly", $params, true);
        return Redirect::to('/polylist');
    }
}
