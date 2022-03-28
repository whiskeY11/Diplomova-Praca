<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class IconItem extends Model
{
    protected $table = "icons";
    public $timestamps = false;
    protected $fillable = [
        "name", "url", "default", "created"
    ];
}
