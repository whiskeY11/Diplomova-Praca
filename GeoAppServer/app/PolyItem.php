<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class PolyItem extends Model
{
    protected $table = "poly";
    public $timestamps = false;
    protected $fillable = [
        "map_id", "name", "json_created", "created"
    ];
}
