<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreateBoundTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::create('bounds', function (Blueprint $table) {
            $table->id();
            $table->double("lat_north");
            $table->double("lat_south");
            $table->double("lng_east");
            $table->double("lng_west");
            $table->BigInteger("created");
        });
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down()
    {
        Schema::dropIfExists('bounds');
    }
}
