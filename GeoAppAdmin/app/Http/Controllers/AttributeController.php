<?php

namespace App\Http\Controllers;

use Illuminate\Routing\Controller;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Redirect;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Session;
use App\Http\Traits\Common;

class AttributeController extends Controller
{
    use Common;

    public function ShowAttributeList() {
        $list = DB::table("attribute")->select(
            "attribute.id as id", "attribute.list_id as list_id","attribute.value as value",
            "attribute.csv_id as csv_id", "list.name as list_name", "list.type as list_type",
            "list.loadjson_id as loadjson_id", "list.city_id as city_id", "city.name as city_name",
            "csv.name as csv_name")
            ->join('list', 'attribute.list_id', '=', 'list.id')
            ->join('csv', 'attribute.csv_id', '=', 'csv.id')
            ->join('city', 'list.city_id', '=', 'city.id')
            ->groupBy('csv_id', 'city_id', 'loadjson_id', 'list_name')
            ->get();

        Session::put("current", "attribute");
        return view("attribute_list", compact("list"));
    }

    public function ShowEditAttribute($id) {
        $item = DB::table("attribute")->select(
            "attribute.id as id", "attribute.list_id as list_id", "attribute.value as value",
            "attribute.csv_id as csv_id", "list.name as list_name", "csv.name as csv_name")
            ->join('list', 'attribute.list_id', '=', 'list.id')
            ->join('csv', 'attribute.csv_id', '=', 'csv.id')
            ->where("attribute.id", $id)->first();
        $csv = DB::table("csv")->get();
        return view("attribute_edit", compact("item", "csv"));
    }

    public function DeleteAttribute($id) {
        $params = array("id" => $id);
        $response = $this->MakeRequest($this->APIPath()."deleteAttribute", $params, true);
        return Redirect::to("/attributelist");
    }

    public function EditAttribute(Request $request) {
        $params = array(
            "id" => $request["id"],
            "csv_id" => $request["csv_id"],
            "value" => $request["value"]
        );
        $this->MakeRequest($this->APIPath()."editAttribute", $params, true);

        return Redirect::to("/attributelist");
    }
}
