<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class CordItem extends Model
{
    protected $table = "cords";
    public $timestamps = false;
    protected $fillable = [
        'loadJson', 'idlist', "lng", "lat"
    ];
}
