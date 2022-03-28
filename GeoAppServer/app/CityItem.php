<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class CityItem extends Model
{
    protected $table = "city";
    public $timestamps = false;
    protected $fillable = [
        "name", "lat", "lng", "map_id", "created"
    ];
}
