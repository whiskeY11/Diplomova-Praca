<?php

namespace App\Http\Controllers;

use App\ListItem;
use App\CordItem;
use App\Traits\Adding;
use App\Traits\Common;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Symfony\Component\HttpKernel\Exception\HttpException;

class AppController extends Controller
{
    use Common, Adding;

    private function isIconAdded($name): bool
    {
        return DB::table('icons')->where('name', $name)->first() != null;
    }

    public function toGeoJSON(Request $request): \Illuminate\Http\JsonResponse
    {
        $geojson = array(
            'type'      => 'FeatureCollection',
            'features'  => array()
        );

        $items = DB::table('list')
            ->select("list.id as id", "list.type as type", "cords.lat as lat", "cords.lng as lng",
                "list.name as name", "list.icon as icon", "list.updated as updated", "list.user_id as user_id",
                "list.loadjson_id as loadjson_id", "list.city_id as city_id")
            ->where('type', $request["type"])
            ->where("deleted", 0)
            ->join('cords', 'list.id', '=', 'cords.idlist')
            ->get();

        $features = array();
        foreach($items as $item) {
            $this->HandleAddingItemToArray($features, $item);
        }

        $geojson['features'] = array_values($features);
        $compressed = gzdeflate(json_encode($geojson),  9);
        $final = base64_encode($compressed);

        return response()->json($final);
    }

    public function toGeoJSONGET($type): \Illuminate\Http\JsonResponse
    {
        echo self::getTimeInMillis()."<br/>";

        $geojson = array(
            'type'      => 'FeatureCollection',
            'features'  => array()
        );

        $items = DB::table('list')
            ->select("list.id as id", "list.type as type", "cords.lat as lat", "cords.lng as lng",
                "list.name as name", "list.icon as icon", "list.updated as updated", "list.user_id as user_id",
                "list.loadjson_id as loadjson_id", "list.city_id as city_id")
            ->where('type', $type)
            ->where("deleted", 0)
            ->join('cords', 'list.id', '=', 'cords.idlist')
            ->get();

        $features = array();
        foreach($items as $item) {
            $this->HandleAddingItemToArray($features, $item);
        }

        $geojson['features'] = array_values($features);
        $compressed = gzdeflate(json_encode($geojson),  9);
        $final = base64_encode($compressed);

        echo self::getTimeInMillis();
        //return response()->json($final);
    }

    public function addItemAndCords(Request $request): \Illuminate\Http\JsonResponse
    {
        $icon = $request["icon"];
        if(!$this->isIconAdded($icon)) {
            $icon = "defaultIcon";
        }

        $itemid = $this->addItem(-1, $request["name"], $request["type"], $icon, $request->user()->id, $request["city_id"]);
        $this->addCords(-1, $itemid, $request["lng"], $request["lat"]);
        return response()->json($this->getFeatureFromID($itemid));
    }

    public function removeItemAndCords(Request $request): \Illuminate\Http\JsonResponse
    {
        $response["result"] = null;
        try {
            $item = ListItem::where("id", $request["id"])->first();
            if($item->type == "LineString") {
                if($item->city_id != -1) {
                    ListItem::where("name", $item->name)
                        ->where("city_id", $item->city_id)
                        ->update(['deleted' => 1, "updated" => $this->getTimeInMillis()]);

                    $list = DB::table("list")->where("city_id", $item->city_id)
                        ->where("name", $item->name)
                        ->pluck('id')->toArray();
                } else {
                    ListItem::where("name", $item->name)
                        ->where("loadjson_id", $item->loadjson_id)
                        ->update(['deleted' => 1, "updated" => $this->getTimeInMillis()]);

                    $list = DB::table("list")->where("loadjson_id", $item->loadjson_id)
                        ->where("name", $item->name)
                        ->pluck('id')->toArray();
                }

                DB::table("attribute")->whereIn("list_id", $list)->delete();
                if(count($list) > 0) CsvController::UpdateUsersAttributeDownload();
            } else {
                ListItem::where("id", $request["id"])->update(['deleted' => 1, "updated" => $this->getTimeInMillis()]);

                $count = DB::table("attribute")->where("list_id", $request["id"])->count();
                DB::table("attribute")->where("list_id", $request["id"])->delete();
                if($count > 0) CsvController::UpdateUsersAttributeDownload();
            }

            if($request->has("admin")) {
                DB::table("list")
                    ->where("id", $request["id"])
                    ->where("user_id", "!=", -1)
                    ->update(array("force_owner_download" => 1));
            }
        } catch (\Exception $e) {
            $response["result"] = $e->getMessage();
            throw new HttpException(500, $e->getMessage());
        }
        return response()->json($response);
    }

    public function editItem(Request $request): \Illuminate\Http\JsonResponse
    {
        $item = ListItem::where("id", $request["id"])->first();
        if($item->type == "LineString") {
            if($item->city_id != -1) {
                ListItem::where("name", $item->name)
                    ->where("city_id", $item->city_id)
                    ->update(['name' => $request["name"], 'icon' => $request["icon"], "updated" => $this->getTimeInMillis()]);
            } else {
                ListItem::where("name", $item->name)
                    ->where("loadjson_id", $item->loadjson_id)
                    ->update(['name' => $request["name"], 'icon' => $request["icon"], "updated" => $this->getTimeInMillis()]);
            }
        } else {
            $icon = $request["icon"];
            if(!$this->isIconAdded($icon)) {
                $icon = "defaultIcon";
            }

            ListItem::where("id", $request["id"])->
                update([
                    'name' => $request["name"],
                    'icon' => $icon,
                    'city_id' => $request["city_id"],
                    "updated" => $this->getTimeInMillis()
                ]);
        }

        if($request->has("admin")) {
            DB::table("list")
                ->where("id", $request["id"])
                ->where("user_id", "!=", -1)
                ->update(array("force_owner_download" => 1));
        }
        return response()->json($this->getFeatureFromID($request["id"]));
    }

    public function editItemLocation(Request $request): \Illuminate\Http\JsonResponse
    {
        CordItem::where("idlist", $request["id"])->update(['lng' => $request["lng"], 'lat' => $request["lat"]]);
        ListItem::where("id", $request["id"])->update(["updated" => $this->getTimeInMillis()]);
        return response()->json($this->getFeatureFromID($request["id"]));
    }

    public function getBounds(): \Illuminate\Http\JsonResponse
    {
        $bounds = DB::table("bounds")->get();

        $array = array();
        foreach($bounds as $row) {
            $bound = array(
                "id" => $row->id,
                "lat_north" => $row->lat_north,
                "lat_south" => $row->lat_south,
                "lng_west" => $row->lng_west,
                "lng_east" => $row->lng_east,
                "last_sync" => $this->getTimeInMillis()
            );

            $array[] = $bound;
        }

        $response["bounds"] = $array;
        $compressed = gzdeflate(json_encode($response),  9);
        $final = base64_encode($compressed);

        return response()->json($final);
    }

    public function getIcons(Request $request): \Illuminate\Http\JsonResponse
    {
        $icons = DB::table("icons")->get();

        $array = array();
        foreach($icons as $row) {
            $url = $row->url;
            $type = substr($url, strrpos($url, ".") + 1);

            if($type == "svg") {
                $type = "VECTOR";
            } else {
                $type = "BITMAP";
            }

            $icon = array(
                "type" => $type,
                "name" => $row->name,
                "url" => $url
            );

            $array[] = $icon;
        }

        $response["icons"] = $array;
        $compressed = gzdeflate(json_encode($response),  9);
        $final = base64_encode($compressed);

        return response()->json($final);
    }

    public function getCities(Request $request): \Illuminate\Http\JsonResponse
    {
        $cities = DB::table("city")->get();

        $geojson = array(
            'type'      => 'FeatureCollection',
            'features'  => array()
        );

        $array = array();
        foreach($cities as $row) {
            $feature = array(
                'type' => 'Feature',
                'geometry' => array(
                    'type' => "Point",
                    'coordinates' => array($row->lng, $row->lat)
                ),
                'properties' => array(
                    'id' => $row->id,
                    'name' => $row->name,
                    'city' => 1
                )
            );

            $array[] = $feature;
        }

        $geojson['features'] = $array;
        $compressed = gzdeflate(json_encode($geojson),  9);
        $final = base64_encode($compressed);

        return response()->json($final);
    }

    public function getAttributes(Request $request): \Illuminate\Http\JsonResponse
    {
        $response["attributes"] = array();
        $response["csv"] = array();
        $response["legends"] = array();

        if($request["attribute_download"] == "1")
        {
            $csv = DB::table("csv")
                ->where("loaded", 1)->get();
            $attributes = DB::table("attribute")->get();

            $array = array();
            foreach ($csv as $row) {
                $array[] = [
                    "id" => $row->id,
                    "legend_id" => $row->legend_id,
                    "city_id" => $row->city_id,
                    "name" => $row->name
                ];
            }
            $response["csv"] = $array;

            $array = array();
            foreach ($attributes as $row) {
                $array[] = [
                    "csv_id" => $row->csv_id,
                    "value" => $row->value,
                    "list_id" => $row->list_id
                ];
            }
            $response["attributes"] = $array;
        }
        if($request["legend_download"] == "1")
        {
            $legends = DB::table("legend")->get();
            $array = array();

            foreach($legends as $row) {
                $array[] = [
                    "id" => $row->id,
                    "name" => $row->name,
                    "zero" => $row->zero,
                    "first" => $row->first,
                    "second" => $row->second,
                    "third" => $row->third,
                    "fourth" => $row->fourth,
                    "fifth" => $row->fifth,
                    "sixth" => $row->sixth,
                    "seventh" => $row->seventh,
                    "eight" => $row->eight,
                    "ninth" => $row->ninth,
                ];
            }

            $response["legends"] = $array;
        }

        $compressed = gzdeflate(json_encode($response),  9);
        $final = base64_encode($compressed);

        return response()->json($final);
    }
}
