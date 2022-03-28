<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreateListTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::create('list', function (Blueprint $table) {
            $table->id();
            $table->integer("loadjson_id");
            $table->string('name')->default("menoitemu");
            $table->string('type');
            $table->string('icon');
            $table->integer('user_id');
            $table->integer('city_id');
            $table->boolean('deleted');
            $table->boolean('force_owner_download')->default(false);
            $table->BigInteger("created");
            $table->BigInteger("updated");
        });
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down()
    {
        Schema::dropIfExists('list');
    }
}
