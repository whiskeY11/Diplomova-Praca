<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class CsvItem extends Model
{
    protected $table = "csv";
    public $timestamps = false;
    protected $fillable = [
        "name", "legend_id", "city_id", "loaded", "created"
    ];
}
