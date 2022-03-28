<?php

namespace App\Traits;

use App\CordItem;
use App\Http\Controllers\CsvController;
use App\ListItem;
use App\BoundItem;
use App\LoadItem;
use Illuminate\Support\Facades\DB;
use OsmPbf\Reader;

trait Adding
{
    use Common;

    private function addAllCords($data)
    {
        DB::table('cords')->insert($data);
    }

    private function addCords($loadJson, $idlist, $lng, $lat)
    {
        $item = new CordItem();
        $item->loadjson_id = $loadJson;
        $item->idlist = $idlist;
        $item->lng = $lng;
        $item->lat = $lat;
        $item->save();
    }

    private function addItem($loadJson, $name, $type, $icon, $userid, $cityid)
    {
        $item = new ListItem();
        $item->loadjson_id = $loadJson;
        $item->name = $name;
        $item->type = $type;
        $item->icon = $icon;
        $item->created = $this->getTimeInMillis();
        $item->updated = $item->created;
        $item->user_id = $userid;
        $item->city_id = $cityid;
        $item->deleted = 0;
        $item->force_owner_download = 0;
        $item->save();

        return $item->id;
    }

    private function addBound($lat_north, $lat_south, $lng_west, $lng_east)
    {
        $bound = new BoundItem();
        $bound->lat_south = $lat_south;
        $bound->lat_north = $lat_north;
        $bound->lng_west = $lng_west;
        $bound->lng_east = $lng_east;
        $bound->created = $this->getTimeInMillis();
        $bound->save();
    }

    private function addLoadItem($poly_id, $name, $type, $iconID, $city_id)
    {
        $item = new LoadItem();
        $item->poly_id = $poly_id;
        $item->city_id = $city_id;
        $item->name = $name;
        $item->type = $type;
        $item->icon = DB::table("icons")->where("id", $iconID)->value("name");
        $item->created = $this->getTimeInMillis();
        $item->save();

        return $item->id;
    }

    //Add ways from PBF JSON data
    public function AddPBFJSONData($id)
    {
        if($this->isLoadEmpty()) {
            return $this->doneResponse();
        }
        if(!$this->isFileAdded($id)){
            return $this->doneResponse();
        }
        if($this->isFileLoaded($id)) {
            return $this->doneResponse();
        }

        $load = DB::table("load")->where("id", $id)->first();
        $ways = $this->ReadFile(storage_path() . '/app/files/'.$load->name.'.'.$load->type);

        foreach($ways as $way) {
            $id = $this->addItem($load->id, $way["name"], "LineString", "defaultIcon", -1, $load->city_id);
            $data = array();
            foreach ($way["nodes"] as $cordRow) {
                $data[] =[
                    'loadjson_id' => $load->id,
                    'idlist' => $id,
                    'lng' => $cordRow["lng"],
                    'lat' => $cordRow["lat"]
                ];
            }
            $this->addAllCords($data);
        }

        DB::table("load")->where("id", $load->id)->update(['loaded' => 1]);

        return $this->doneResponse();
    }

    public function addBounds(): \Illuminate\Http\JsonResponse
    {
        $nazov = "bounds.json";
        echo "Loading ".$nazov."...\n";

        DB::table("bounds")->delete();

        $path = storage_path() . "/app/${nazov}";
        $json = $this->ReadFile($path);

        $userData = $json['features'];
        foreach($userData as $outerRow) {
            $geometry = $outerRow['geometry'];
            $corArray = $geometry["coordinates"];

            foreach($corArray as $row) {
                $firstPoint  = $row[0];
                $thirdPoint = $row[2];

                $lng_west = $firstPoint[0];
                $lng_east = $thirdPoint[0];
                if($lng_east < $lng_west) {
                    $pom = $lng_west;
                    $lng_west = $lng_east;
                    $lng_east = $pom;
                }

                $this->addBound($firstPoint[1], $thirdPoint[1], $lng_west, $lng_east);
                break;
            }
        }

        return $this->doneResponse();
    }

    public function ParseCitiesFromMap($map_id): \Illuminate\Http\JsonResponse
    {
        $ids = DB::table("city")->where("map_id", $map_id)->pluck("id")->toArray();
        DB::table("list")->whereIn("city_id", $ids)->update(array(
            "city_id" => -1,
            "updated" => $this->getTimeInMillis()
        ));
        DB::table("load")->whereIn("city_id", $ids)->update(array(
            "city_id" => -1
        ));
        DB::table("poly")->whereIn("city_id", $ids)->update(array(
            "city_id" => -1
        ));
        DB::table("csv")->whereIn("city_id", $ids)->update(array(
            "city_id" => -1
        ));

        DB::table("city")->where("map_id", $map_id)->delete();

        $path = storage_path()."/app/";
        $this->RemoveFile($path."temp.osm");
        $this->RemoveFile($path."cities.osm");
        $this->RemoveFile($path."cities.pbf");

        $map = DB::table("maps")->where("id", $map_id)->first();

        exec('cd '.$path.'&&'.' osmconvert maps/'.$map->name.'.'.$map->type." -o=temp.osm");
        exec('cd '.$path.'&&'.' osmfilter temp.osm --keep="place=city or ( place=town and population>=10000 )" >cities.osm');
        exec('cd '.$path.'&&'.' osmconvert cities.osm -o=cities.pbf');

        $this->RemoveFile($path."temp.osm");
        $this->RemoveFile($path."cities.osm");

        $file_handler = fopen($path."cities.pbf", "rb");
        $pbfreader = new Reader($file_handler);

        ini_set('memory_limit', '-1');
        $pbfreader->skipToBlock(0);
        while ($pbfreader->next()) {
            $elements = $pbfreader->getElements();
            if($elements != null) {
                if($elements["type"] == "node") {
                    $cities = [];

                    foreach ($elements['data'] as $element) {
                        if(isset($element["tags"]["name"])) {
                            $insert_obj = [
                                "name" => $element["tags"]["name"]["value"],
                                "lat" => $element["latitude"],
                                "lng" => $element["longitude"],
                                "map_id" => $map->id,
                                "created" => $this->getTimeInMillis()
                            ];
                            $cities[] = $insert_obj;
                        }
                    }

                    DB::table('city')->insert($cities);
                }
            }
        }

        $this->RemoveFile($path."cities.pbf");
        return $this->doneResponse();
    }

    public function LoadDataFromCSV($id, $forceUpdate = false) : array
    {
        $csv = DB::table("csv")->where("id", $id)->first();
        $notFoundItems = array();
        $foundItems = array();

        $path = storage_path() . "/app/csv/" . $csv->name . ".csv";
        if (file_exists($path)) {
            $file = fopen($path, 'r');
        } else {
            DB::table("csv")->where("id", $id)->delete();
            return $notFoundItems;
        }

        $data = array();
        while (($line = fgetcsv($file, 0,";")) !== FALSE) {
            $data[$line[0]] = [
                "key" => $line[0],
                "value" => str_replace(' ', '', $line[1]) //for empty spaces
            ];
        }
        fclose($file);

        foreach($data as $row) {
            $item = DB::table("list")
                ->where("name", $row["key"])
                ->where("city_id", $csv->city_id)
                ->where("type", "LineString")
                ->get();
            if(count($item) > 0) {
                foreach($item as $one) {
                    $foundItems[] = [
                        "list_id" => $one->id,
                        "value" => intval($row["value"]),
                        "csv_id" => $csv->id,
                        "created" => $this->getTimeInMillis()
                    ];
                }
            } else {
                $notFoundItems[] = $row["key"];
            }
        }

        DB::table('attribute')->insert($foundItems);

        DB::table("csv")->where("id", $csv->id)
            ->update(array(
                "loaded" => 1
            ));

        return $notFoundItems;
    }
}

