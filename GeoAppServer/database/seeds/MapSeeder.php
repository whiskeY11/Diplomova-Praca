<?php

use App\Http\Controllers\MapController;
use App\Traits\Adding;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;

class MapSeeder extends Seeder
{
    use Adding;

    public function run()
    {
        DB::table("maps")->delete();
        DB::table("legend")->delete();

        DB::table('maps')->insert([
            'name' => "slovakia",
            'type' => "osm.pbf",
            'default' => 1,
            "created" => $this->getTimeInMillis()
        ]);

        DB::table('legend')->insert([
            'name' => MapController::$MapLegendDefault,
            "zero" => "#a7f542",
            "first" => "#81f542",
            "second" => "#42b6f5",
            "third" => "#4275f5",
            "fourth" => "#cef542",
            "fifth" => "#f5d742",
            "sixth" => "#f5c242",
            "seventh" => "#f5ad42",
            "eight" => "#f57842",
            "ninth" => "#f54242",
            "default" => 1,
            "created" => $this->getTimeInMillis()
        ]);
    }
}
