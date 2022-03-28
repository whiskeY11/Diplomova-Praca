<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class LegendItem extends Model
{
    protected $table = "legend";
    public $timestamps = false;
    protected $fillable = [
        "name", "zero", "first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eight", "ninth",  "default", "created"
    ];
}
