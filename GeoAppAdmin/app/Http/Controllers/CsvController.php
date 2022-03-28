<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Routing\Controller;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Redirect;
use App\Http\Traits\Common;
use Illuminate\Support\Facades\Session;
use Illuminate\Support\Facades\Storage;

class CsvController extends Controller
{
    use Common;

    public function ShowCSVList() {
        $list = DB::table("csv")
            ->select("csv.id as id", "csv.name as name", "csv.loaded as loaded",
                "csv.city_id as city_id", "legend.name as legend")
            ->join('legend', 'csv.legend_id', '=', 'legend.id')
            ->get();
        $legends = DB::table("legend")->get();
        $cities = DB::table("city")
            ->orderBy('name', 'ASC')
            ->get();
        foreach($list as $row) {
            if($row->city_id != -1) {
                $row->city_id = DB::table("city")->where("id", $row->city_id)->value("name");
            }
        }
        Session::put("current", "csv");
        return view("csv_list", compact("list", "legends", "cities"));
    }

    public function ShowEditCSV($id) {
        $csv = DB::table("csv")->where("id", $id)->first();
        $legends = DB::table("legend")->get();
        $cities = DB::table("city")
            ->orderBy('name', 'ASC')
            ->get();
        Session::forget("notfound");
        return view("csv_edit", compact("csv", "legends", "cities"));
    }

    public function DeleteCSV($id) {
        $params = array("id" => $id);
        $this->MakeRequest($this->APIPath()."removeCsv", $params, true);
        Session::forget("notfound");
        return Redirect::to("/csvlist");
    }

    public function UploadCSV(Request $request) {
        ini_set('memory_limit', '-1');

        $file = $request->file('file');
        $file_name = $file->getClientOriginalName();
        $data = $this->Compress(file_get_contents($file));

        $params = array(
            "name" => strtok($file_name, "."),
            "data" => $data,
            "legend" => $request["legend"],
            "city" => $request["city"],
            "load" => $request->has("load") ? 1 : 0
        );
        $response = $this->MakeRequest($this->APIPath()."addCsv", $params, true);
        $this->HandleNotFoundItems(strtok($file_name, "."), $response["notfound"]);
        return Redirect::to('/csvlist');
    }

    public function EditCSV(Request $request) {
        $params = array(
            "id" => $request["id"],
            "name" => $request["name"],
            "legend" => $request["legend"],
            "city_id" => $request["city"],
            "load" => $request->has("load") ? 1 : 0
        );
        $response = $this->MakeRequest($this->APIPath()."editCsv", $params, true);
        $this->HandleNotFoundItems($request["name"], $response["notfound"]);
        return Redirect::to('/csvlist');
    }

    private function HandleNotFoundItems($name, $notfound)
    {
        if(count($notfound) == 0)
        {
            Session::forget("notfound");
        }
        else
        {
            $array = array();
            $array[] = $name;
            foreach($notfound as $row) {
                $array[] = $row;
            }
            Session::put("notfound", $array);
        }
    }

    public function DumpNotFound()
    {
        Session::forget("notfound");
        return Redirect::to('/csvlist');
    }
}
