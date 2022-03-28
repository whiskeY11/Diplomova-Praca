<?php

namespace App\Http\Controllers;

use Illuminate\Routing\Controller;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Redirect;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Session;
use App\Http\Traits\Common;

class ListController extends Controller
{
    use Common;

    public function ShowList($type) {
        if($type == "all") {
            $list = DB::table("list")
                ->where("deleted", 0)
                ->groupBy('city_id', 'loadjson_id', 'name')
                ->get();
        } else {
            $list = DB::table("list")
                ->where("type", $type)
                ->where("deleted", 0)
                ->groupBy('city_id', 'loadjson_id', 'name')
                ->get();

        }

        $icons = DB::table("icons")->get();

        $iconsArray = array();
        foreach($icons as $row) {
            $iconsArray[$row->name] = $this->APIStoragePath().$row->url;
        }

        foreach($list as $row) {
            if($row->user_id != -1) {
                $row->user_id = DB::table("users")->where("id", $row->user_id)->value("email");
            }
            if($row->loadjson_id != -1) {
                $row->loadjson_id = DB::table("load")->where("id", $row->loadjson_id)->value("name");
            }
            if($row->city_id != -1) {
                $row->city_id = DB::table("city")->where("id", $row->city_id)->value("name");
            }
        }

        Session::put("type", $type);
        Session::put("current", $type);

        return view("list", compact("list", "iconsArray"));
    }

    public function ShowEditListItem($id) {
        $item = DB::table("list")->where("id", $id)->first();
        $icons = DB::table("icons")->get();
        $cities = DB::table("city")
            ->orderBy('name', 'ASC')
            ->get();

        $iconsName = array();
        foreach($icons as $row) {
            $row->url = $this->APIStoragePath().$row->url;
            $iconsName[$row->name] = $row->url;
        }
        return view("list_edit", compact("item", "icons", "iconsName", "cities"));
    }

    public function DeleteListItem($id) {
        $params = array("id" => $id, "admin" => true);
        $response = $this->MakeRequest($this->APIPath()."removeItemAndCords", $params, true);
        return Redirect::to("/list/".Session::get("type", "all"));
    }

    public function EditListItem(Request $request) {
        $params = array(
            "id" => $request["id"],
            "name" => $request["name"],
            "city_id" => $request["city"],
            "icon" => $request["icon"],
            "admin" => true
        );
        $this->MakeRequest($this->APIPath()."editItem", $params, true);

        return Redirect::to("/list/".Session::get("type", "all"));
    }

    public function ReimportAllData() {
        $response = $this->MakeRequest($this->APIPath()."reimportAll", null, true);
        return Redirect::to("/list/".Session::get("type", "all"));
    }
}
