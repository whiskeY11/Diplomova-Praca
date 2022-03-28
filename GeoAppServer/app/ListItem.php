<?php

namespace App;

use Illuminate\Database\Eloquent\Model;

class ListItem extends Model
{
    protected $table = "list";
    public $timestamps = false;
    protected $fillable = [
         'name', 'type', "icon", "created", "updated", "user_id", "city_id", "deleted"
    ];
}
