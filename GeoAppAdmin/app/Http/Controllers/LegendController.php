<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Routing\Controller;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Redirect;
use App\Http\Traits\Common;
use Illuminate\Support\Facades\Session;

class LegendController extends Controller
{
    use Common;

    public function ShowLegendList() {
        $list = DB::table("legend")->get();
        Session::put("current", "legend");
        return view("legend_list", compact("list"));
    }

    public function ShowEditLegend($id) {
        $legend = DB::table("legend")->where("id", $id)->first();
        return view("legend_edit", compact("legend"));
    }

    public function DeleteLegend($id) {
        $params = array("id" => $id);
        $response = $this->MakeRequest($this->APIPath()."removeMapLegend", $params, true);
        return Redirect::to("/legendlist");
    }

    public function InsertLegend(Request $request) {
        $colors = array(
            $request["zero"],
            $request["first"],
            $request["second"],
            $request["third"],
            $request["fourth"],
            $request["fifth"],
            $request["sixth"],
            $request["seventh"],
            $request["eight"],
            $request["ninth"],
        );

        $params = array(
            "name" => $request["name"],
            "colors" => $colors
        );
        $this->MakeRequest($this->APIPath()."addMapLegend", $params, true);

        return Redirect::to("/legendlist");
    }

    public function EditLegend(Request $request) {
        $colors = array(
            $request["zero"],
            $request["first"],
            $request["second"],
            $request["third"],
            $request["fourth"],
            $request["fifth"],
            $request["sixth"],
            $request["seventh"],
            $request["eight"],
            $request["ninth"],
        );

        $params = array(
            "id" => $request["id"],
            "name" => $request["name"],
            "colors" => $colors
        );
        $this->MakeRequest($this->APIPath()."editMapLegend", $params, true);
        return Redirect::to("/legendlist");
    }
}
