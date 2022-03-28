<?php

use App\Traits\Common;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;

class IconSeeder extends Seeder
{
    use Common;

    public function run()
    {
        DB::table("icons")->delete();

        DB::table('icons')->insert([
            'name' => "defaultIcon",
            'url' => "icons/defaultIcon.png",
            'default' => true,
            "created" => $this->getTimeInMillis()
        ]);
        DB::table('icons')->insert([
            'name' => "water",
            'url' => "icons/water.png",
            'default' => true,
            "created" => $this->getTimeInMillis()
        ]);
        DB::table('icons')->insert([
            'name' => "recycling",
            'url' => "icons/recycling.png",
            'default' => true,
            "created" => $this->getTimeInMillis()
        ]);
        DB::table('icons')->insert([
            'name' => "shop",
            'url' => "icons/shop.png",
            'default' => true,
            "created" => $this->getTimeInMillis()
        ]);
        DB::table('icons')->insert([
            'name' => "toilet",
            'url' => "icons/toilet.png",
            'default' => true,
            "created" => $this->getTimeInMillis()
        ]);
        DB::table('icons')->insert([
            'name' => "fountain",
            'url' => "icons/fountain.png",
            'default' => true,
            "created" => $this->getTimeInMillis()
        ]);
    }
}
