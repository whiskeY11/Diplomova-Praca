<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Routing\Controller;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Redirect;
use App\Http\Traits\Common;
use Illuminate\Support\Facades\Session;
use Illuminate\Support\Facades\Storage;

class MapController extends Controller
{
    use Common;

    public function ShowMapList() {
        $list = DB::table("maps")->get();
        Session::put("current", "map");
        return view("map_list", compact("list"));
    }

    public function ShowEditMap($id) {
        $map = DB::table("maps")->where("id", $id)->first();
        return view("map_edit", compact("map"));
    }

    public function ShowUpdateMap($id) {
        $map = DB::table("maps")->where("id", $id)->first();
        return view("map_update", compact("map"));
    }

    public function DeleteMap($id) {
        $params = array("id" => $id, "removeFromDB" => 1);
        $response = $this->MakeRequest($this->APIPath()."removeMap", $params, true);
        return Redirect::to("/maplist");
    }

    public function UploadMap(Request $request) {
        ini_set('memory_limit', '-1');

        $file = $request->file('file');
        $file_name = $file->getClientOriginalName();
        Storage::disk('map')->put($file_name, file_get_contents($file));

        $params = array(
            "name" => strtok($file_name, "."),
            "type" => substr($file_name, strpos($file_name, ".") + 1)
        );
        $this->MakeRequest($this->APIPath()."addMap", $params, true);

        return Redirect::to('/maplist');
    }

    public function UpdateMap(Request $request) {
        $params = array("id" => $request["id"], "removeFromDB" => 0);
        $this->MakeRequest($this->APIPath()."removeMap", $params, true);

        ini_set('memory_limit', '-1');

        $file = $request->file('file');
        $map = DB::table("maps")->where("id", $request["id"])->first();
        Storage::disk('map')->put($map->name.".".$map->type, file_get_contents($file));

        return Redirect::to('/maplist');
    }

    public function EditMap(Request $request) {
        $params = array(
            "id" => $request["id"],
            "name" => $request["name"]
        );
        $this->MakeRequest($this->APIPath()."editMap", $params, true);

        return Redirect::to('/maplist');
    }
}
