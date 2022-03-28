<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class MapItem extends Model
{
    protected $table = "maps";
    public $timestamps = false;
    protected $fillable = [
        "name", "created"
    ];
}
