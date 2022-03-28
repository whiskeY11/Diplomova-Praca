<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreateLoadTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::create('load', function (Blueprint $table) {
            $table->id();
            $table->integer("poly_id");
            $table->integer("city_id");
            $table->string('name');
            $table->string('type');
            $table->string('icon');
            $table->boolean('loaded')->default(0);
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
        Schema::dropIfExists('load');
    }
}
