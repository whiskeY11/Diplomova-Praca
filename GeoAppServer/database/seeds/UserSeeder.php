<?php

use App\Traits\Common;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;

class UserSeeder extends Seeder
{
    use Common;

    public function run()
    {
        DB::table("users")->delete();

        DB::table('users')->insert([
            'email' => "admin",
            "password" => Hash::make("admin"),
            "role_id" => 2,
            "force_download" => 0,
            "icon_download" => 0,
            "attribute_download" => 0,
            "city_download" => 0,
            "legend_download" => 0,
            "last_login_time" => 0
        ]);
    }
}
