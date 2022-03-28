<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class BoundItem extends Model
{
    protected $table = "bounds";
    public $timestamps = false;
    protected $fillable = [
        "lat_south", "lat_north", "lng_west", "lng_east"
    ];
}
