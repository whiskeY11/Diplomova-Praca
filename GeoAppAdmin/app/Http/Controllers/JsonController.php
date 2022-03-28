<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Routing\Controller;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Redirect;
use App\Http\Traits\Common;
use Illuminate\Support\Facades\Session;

class JsonController extends Controller
{
    use Common;

    public function ShowEditFile($id) {
        $file = DB::table("load")->where("id", $id)->first();
        $icons = DB::table("icons")->get();
        $cities = DB::table("city")
            ->orderBy('name', 'ASC')
            ->get();

        $iconsId = array();
        foreach($icons as $row) {
            $row->url = $this->APIStoragePath().$row->url;
            $iconsId[$row->id] = $row->url;
        }
        return view("file_edit", compact("file", "icons", "iconsId", "cities"));
    }

    public function ShowFileList() {
        $list = DB::table("load")->get();
        $icons = DB::table("icons")->get();
        $cities = DB::table("city")
            ->orderBy('name', 'ASC')
            ->get();

        $iconsName = array();
        $iconsId = array();
        foreach($icons as $row) {
            $row->url = $this->APIStoragePath().$row->url;
            $iconsName[$row->name] = $row->url;
            $iconsId[$row->id] = $row->url;
        }

        foreach($list as $row) {
            if($row->poly_id != -1) {
                $row->poly_id = DB::table("poly")->where("id", $row->poly_id)->value("name");
            }
            if($row->city_id != -1) {
                $row->city_id = DB::table("city")->where("id", $row->city_id)->value("name");
            }
        }

        Session::put("current", "file");
        return view("file_list", compact("list", "icons", "iconsName", "iconsId", "cities"));
    }

    public function DeleteFile($id) {
        $params = array("id" => $id);
        $this->MakeRequest($this->APIPath()."removeJson", $params, true);
        return Redirect::to("/filelist");
    }

    public function UploadFile(Request $request) {
        $file = $request->file('file');
        $file_name = $file->getClientOriginalName();
        $data = $this->Compress(file_get_contents($file));

        $params = array(
            "name" => strtok($file_name, "."),
            "type" => substr($file_name, strrpos($file_name, ".") + 1),
            "data" => $data,
            "iconID" => $request["icon"],
            "cityID" => $request["city"],
            "load" => $request->has("load") ? 1 : 0
            );
        $response = $this->MakeRequest($this->APIPath()."addFile", $params, true);
        return Redirect::to('/filelist');
    }

    public function EditFile(Request $request) {
        $params = array(
            "id" => $request["id"],
            "name" => $request["name"],
            "iconID" => $request["icon"],
            "city_id" => $request["city"],
            "load" => $request->has("load") ? 1 : 0
        );
        $response = $this->MakeRequest($this->APIPath()."editFile", $params, true);
        return Redirect::to('/filelist');
    }
}
