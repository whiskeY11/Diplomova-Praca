<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

class CreateUsersTable extends Migration
{
    /**
     * Run the migrations.
     *
     * @return void
     */
    public function up()
    {
        Schema::create('users', function (Blueprint $table) {
            $table->id();
            $table->string("email");
            $table->string("password");
            $table->integer("role_id");
            $table->boolean("force_download");
            $table->boolean("icon_download");
            $table->boolean("attribute_download");
            $table->boolean("city_download");
            $table->boolean("legend_download");
            $table->bigInteger("last_login_time");
        });
    }

    /**
     * Reverse the migrations.
     *
     * @return void
     */
    public function down()
    {
        Schema::dropIfExists('users');
    }
}
