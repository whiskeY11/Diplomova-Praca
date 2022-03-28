<?php

namespace App\Traits;

use App\Http\Controllers\CsvController;
use Illuminate\Support\Facades\DB;

trait Common
{
    private function doneResponse(): \Illuminate\Http\JsonResponse
    {
        $response["result"] = "done";
        return response()->json($response);
    }

    private static function getTimeInMillis(): float
    {
        return round(microtime(true) * 1000);
    }

    private function getAllCords($idlist): \Illuminate\Support\Collection
    {
        return DB::table("cords")->select("lng", "lat")->where("idlist", $idlist)->get();
    }

    private function isFileLoaded($id): bool
    {
        return DB::table('load')->select("loaded")->where('id', $id)->first()->loaded == 1;
    }

    private function isFileAdded($id): bool
    {
        return DB::table('load')->where('id', $id)->first() != null;
    }

    private function isFileDuplicate($name): bool
    {
        return DB::table('load')->where('name', $name)->first() != null;
    }

    private function isLoadEmpty(): bool
    {
        return DB::table("load")->count() == 0;
    }

    private function getFeatureFromID($id): array
    {
        $item = DB::table('list')->where('id', $id)->first();

        $feature = array(
            'type' => 'Feature',
            'geometry' => array(
                'type' => $item->type,
                'coordinates' => null //will be changed later based on type
            ),
            'properties' => array(
                'name' => $item->name,
                'icon' => $item->icon,
                'selected' => false,
                'value' => 0,
                'id' => $item->id,
                'updated' => $item->updated,
                'userid' => $item->user_id,
                'loadid' => $item->loadjson_id,
                'cityid' => $item->city_id
            )
        );

        $cords = $this->getAllCords($item->id);

        switch($item->type) {
            case "Point":
                $lng = 0;
                $lat = 0;
                foreach ($cords as $row) {
                    $lng = $row->lng;
                    $lat = $row->lat;
                }
                $feature["geometry"]["coordinates"] = array($lng, $lat);
                break;
            case "LineString":
                $array = array();
                foreach ($cords as $row) {
                    $array[] = array($row->lng,$row->lat);
                }
                $feature["geometry"]["coordinates"] = $array;
                break;
            case "Polygon":
                $finalarray = array();
                $array = array();
                foreach ($cords as $row) {
                    $array[] = array($row->lng,$row->lat);
                }
                $finalarray[] = $array;
                $feature["geometry"]["coordinates"] = $finalarray;
                break;
        }

        return $feature;
    }

    /*private function getFeaturesFromList($list): array
    {
        $items = DB::table('list')->whereIn('id', $list)->get();

        $features = array();
        foreach($items as $item) {
            $feature = array(
                'type' => 'Feature',
                'geometry' => array(
                    'type' => $item->type,
                    'coordinates' => null //will be changed later based on type
                ),
                'properties' => array(
                    'name' => $item->name,
                    'icon' => $item->icon,
                    'selected' => false,
                    'value' => 0,
                    'id' => $item->id,
                    'updated' => $item->updated,
                    'userid' => $item->user_id,
                    'loadid' => $item->loadjson_id,
                    'cityid' => $item->city_id
                )
            );

            $cords = $this->getAllCords($item->id);

            switch($item->type) {
                case "Point":
                    $lng = 0;
                    $lat = 0;
                    foreach ($cords as $row) {
                        $lng = $row->lng;
                        $lat = $row->lat;
                    }
                    $feature["geometry"]["coordinates"] = array($lng, $lat);
                    break;
                case "LineString":
                    $array = array();
                    foreach ($cords as $row) {
                        $array[] = array($row->lng,$row->lat);
                    }
                    $feature["geometry"]["coordinates"] = $array;
                    break;
                case "Polygon":
                    $finalarray = array();
                    $array = array();
                    foreach ($cords as $row) {
                        $array[] = array($row->lng,$row->lat);
                    }
                    $finalarray[] = $array;
                    $feature["geometry"]["coordinates"] = $finalarray;
                    break;
            }

            $features[] = $feature;
        }

        return $features;
    }*/

    private function HandleAddingItemToArray(&$arrayToAdd, $item) {
        if(isset($arrayToAdd[$item->id])) {
            switch ($item->type) {
                case "LineString":
                    $array = array($item->lng, $item->lat);
                    $arrayToAdd[$item->id]["geometry"]["coordinates"][] = $array;
                    break;
                case "Polygon":
                    $array = array($item->lng, $item->lat);
                    $arrayToAdd[$item->id]["geometry"]["coordinates"][0][] = $array;
                    break;
            }
        } else {
            $feature = array(
                'type' => 'Feature',
                'geometry' => array(
                    'type' => $item->type,
                    'coordinates' => null //will be changed later based on type
                ),
                'properties' => array(
                    'name' => $item->name,
                    'icon' => $item->icon,
                    'selected' => false,
                    'value' => 0,
                    'id' => $item->id,
                    'updated' => $item->updated,
                    'userid' => $item->user_id,
                    'loadid' => $item->loadjson_id,
                    'cityid' => $item->city_id
                )
            );

            switch ($item->type) {
                case "Point":
                    $array = array($item->lng, $item->lat);
                    $feature["geometry"]["coordinates"] = $array;
                    break;
                case "LineString":
                    $array = array(array($item->lng, $item->lat));
                    $feature["geometry"]["coordinates"] = $array;
                    break;
                case "Polygon":
                    $array = array(array(array($item->lng, $item->lat)));
                    $feature["geometry"]["coordinates"] = $array;
                    break;
            }

            $arrayToAdd[$item->id] = $feature;
        }
    }

    private function removeGeoJSONData($id): \Illuminate\Http\JsonResponse
    {
        if($this->isLoadEmpty()) {
            return $this->doneResponse();
        }
        if(!$this->isFileAdded($id)){
            return $this->doneResponse();
        }
        if(!$this->isFileLoaded($id)) {
            return $this->doneResponse();
        }

        echo "Removing all added data from FILE with ID: ".$id."...\n";

        DB::table('list')->where('loadjson_id', $id)->update(['deleted' => 1, 'updated' => $this->getTimeInMillis()]);
        DB::table("load")->where("id", $id)->update(['loaded' => 0]);

        $city_id = DB::table("load")
            ->where("id", $id)
            ->where("poly_id", "!=", -1)
            ->value("city_id");
        DB::table("csv")->where("city_id", $city_id)->update(array("loaded" => 0));

        $list = DB::table("list")->where("loadjson_id", $id)->pluck('id')->toArray();
        $countDeleted = DB::table("attribute")->whereIn("list_id", $list)->delete();
        if($countDeleted > 0) CsvController::UpdateUsersAttributeDownload();

        return $this->doneResponse();
    }

    private function SaveFile($path, $value, bool $encode = true) {
        if (file_exists($path))
        {
            unlink($path);
        }

        $fp = fopen($path, 'w');
        $encode ? fwrite($fp, json_encode($value, JSON_UNESCAPED_UNICODE)) : fwrite($fp, $value);
        fclose($fp);
    }

    private function ReadFile($path) {
        if (file_exists($path))
        {
            return json_decode(file_get_contents($path), true);
        }

        return null;
    }

    private function RemoveFile($path) {
        if (file_exists($path))
        {
            unlink($path);
        }
    }

    private function RenameFile($from, $to) {
        if (file_exists($from))
        {
            rename($from, $to);
        }
    }

    private function Decompress($compressed) {
        $decoded = base64_decode($compressed);
        $uncompressed = gzinflate($decoded);
        $data = gzinflate($uncompressed);
        return $data;
    }
}

