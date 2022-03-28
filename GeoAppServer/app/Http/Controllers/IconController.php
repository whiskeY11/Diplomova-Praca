<?php

namespace App\Http\Controllers;

use App\IconItem;
use App\Traits\Common;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class IconController extends Controller
{
    use Common;

    private function IsIconDuplicate($name): bool
    {
        return DB::table('icons')->where('name', $name)->first() != null;
    }

    private function IsIconAdded($id): bool
    {
        return DB::table('icons')->where('id', $id)->first() != null;
    }

    public function InsertIcon(Request $request)
    {
        if($this->IsIconDuplicate($request["name"]))
        {
            return $this->doneResponse();
        }

        $data = $this->Decompress($request["data"]);
        $this->SaveFile(storage_path()."/app/icons/".$request["name"].".".$request["type"], $data, false);

        $url = "icons/".$request["name"].".".$request["type"];

        $item = new IconItem();
        $item->name = $request["name"];
        $item->url = $url;
        $item->default = 0;
        $item->created = $this->getTimeInMillis();
        $item->save();

        $this->UpdateUsersIconDownload();

        return $this->doneResponse();
    }

    public function EditIcon(Request $request)
    {
        if(!$this->IsIconAdded($request["id"]))
        {
            return $this->doneResponse();
        }

        $icon = DB::table("icons")->where("id", $request["id"])->first();

        if($icon->name != $request["name"]) {
            if($this->IsIconDuplicate($request["name"]))
            {
                return $this->doneResponse();
            }

            DB::table("load")->where("icon", $icon->name)
                ->update(array(
                    "icon" => $request["name"]
                ));

            DB::table("list")
                ->where("icon", $icon->name)
                ->where("user_id", "!=", -1)
                ->update(array("force_owner_download" => 1));

            DB::table("list")->where("icon", $icon->name)
                ->update(array(
                    "icon" => $request["name"],
                    "updated" => $this->getTimeInMillis()
                ));

            DB::table("icons")->where("id", $request["id"])->
                update(array(
                    "name" => $request["name"]
                ));
        }

        $this->UpdateUsersIconDownload();

        return $this->doneResponse();
    }

    public function RemoveIcon(Request $request)
    {
        if(!$this->IsIconAdded($request["id"]))
        {
            return $this->doneResponse();
        }

        $icon = DB::table("icons")->where("id", $request["id"])->first();

        $this->RemoveFile(storage_path()."/app/".$icon->url);

        DB::table("load")->where("icon", $icon->name)
            ->update(array(
                "icon" => "defaultIcon"
            ));

        DB::table("list")
            ->where("icon", $icon->name)
            ->where("user_id", "!=", -1)
            ->update(array("force_owner_download" => 1));

        DB::table("list")->where("icon", $icon->name)
            ->update(array(
                "icon" => "defaultIcon",
                "updated" => $this->getTimeInMillis()
            ));

        DB::table("icons")->where("id", $request["id"])->delete();

        $this->UpdateUsersIconDownload();

        return $this->doneResponse();
    }

    private function UpdateUsersIconDownload()
    {
        DB::table("users")->where("role_id", 1)
            ->update(array(
                "icon_download" => 1
            ));
    }
}
