<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class AttributeItem extends Model
{
    protected $table = "attribute";
    public $timestamps = false;
    protected $fillable = [
        "list_id", "value", "created"
    ];
}
