<?php

use App\Traits\Adding;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;

class BoundSeeder extends Seeder
{
    use Adding;

    public function run()
    {
        DB::table("bounds")->delete();

        $this->addBounds();
    }
}
