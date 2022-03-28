<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreatePolyTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::create('poly', function (Blueprint $table) {
            $table->id();
            $table->integer("map_id");
            $table->integer("city_id");
            $table->string('name');
            $table->boolean('json_created')->default(0);
            $table->bigInteger("created");
        });
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down()
    {
        Schema::dropIfExists('poly');
    }
}
