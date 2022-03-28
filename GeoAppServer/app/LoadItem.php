<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class LoadItem extends Model
{
    protected $table = "load";
    public $timestamps = false;
    protected $fillable = [
        "name", "poly_id", "city_id", "type", "icon", "loaded", "created"
    ];
}
