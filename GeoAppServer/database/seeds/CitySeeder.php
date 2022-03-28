<?php

use App\Traits\Adding;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;
use OsmPbf\Reader;

class CitySeeder extends Seeder
{
    use Adding;

    public function run()
    {
        DB::table("city")->delete();
        $map = DB::table("maps")->where("name", "slovakia")->first();
        $this->ParseCitiesFromMap($map->id);
    }
}
