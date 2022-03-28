<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Routing\Controller;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Redirect;
use App\Http\Traits\Common;
use Illuminate\Support\Facades\Session;
use Illuminate\Support\Facades\Storage;

class IconController extends Controller
{
    use Common;

    public function ShowIconList() {
        $list = DB::table("icons")->get();
        foreach($list as $row) {
            $row->url = $this->APIStoragePath().$row->url;
        }
        Session::put("current", "icon");
        return view("icon_list", compact("list"));
    }

    public function ShowEditIcon($id) {
        $icon = DB::table("icons")->where("id", $id)->first();
        return view("icon_edit", compact("icon"));
    }

    public function DeleteIcon($id) {
        $params = array("id" => $id);
        $response = $this->MakeRequest($this->APIPath()."removeIcon", $params, true);
        return Redirect::to("/iconlist");
    }

    public function UploadIcon(Request $request) {
        ini_set('memory_limit', '-1');

        $file = $request->file('file');
        $file_name = $file->getClientOriginalName();
        $data = $this->Compress(file_get_contents($file));

        $params = array(
            "name" => strtok($file_name, "."),
            "type" => substr($file_name, strpos($file_name, ".") + 1),
            "data" => $data
        );
        $this->MakeRequest($this->APIPath()."addIcon", $params, true);

        return Redirect::to('/iconlist');
    }

    public function EditIcon(Request $request) {
        $params = array(
            "id" => $request["id"],
            "name" => $request["name"]
        );
        $this->MakeRequest($this->APIPath()."editIcon", $params, true);

        return Redirect::to('/iconlist');
    }
}
