<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreateLegendTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::create('legend', function (Blueprint $table) {
            $table->id();
            $table->string("name");
            $table->string("zero");
            $table->string("first");
            $table->string("second");
            $table->string("third");
            $table->string("fourth");
            $table->string("fifth");
            $table->string("sixth");
            $table->string("seventh");
            $table->string("eight");
            $table->string("ninth");
            $table->boolean("default");
            $table->bigInteger('created');
        });
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down()
    {
        Schema::dropIfExists('legend');
    }
}
