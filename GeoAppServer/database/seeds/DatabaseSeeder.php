<?php

use Illuminate\Database\Seeder;

class DatabaseSeeder extends Seeder
{
    /**
     * Run the database seeds.
     *
     * @return void
     */
    public function run()
    {
        $this->call(IconSeeder::class);
        $this->call(UserSeeder::class);
        $this->call(BoundSeeder::class);
        $this->call(MapSeeder::class);
        $this->call(CitySeeder::class);
    }
}
