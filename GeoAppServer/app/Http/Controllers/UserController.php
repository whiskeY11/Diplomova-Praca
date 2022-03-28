<?php

namespace App\Http\Controllers;

use App\User;
use DateTime;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use App\Traits\Common;
use Symfony\Component\HttpKernel\Exception\HttpException;

class UserController extends Controller {
    use Common;

    private static $userExistsCode = "1";

    private function IsUserDuplicate($email): bool
    {
        return DB::table('users')->where('email', $email)->first() != null;
    }

    public function registerUser(Request $request): \Illuminate\Http\JsonResponse
    {
        if($this->IsUserDuplicate($request["email"]))
        {
            $response["result"] = self::$userExistsCode;
            return response()->json($response);
        }

        $user = new User();
        $user->email = $request["email"];
        $user->password = Hash::make($request["password"]);
        $user->role_id = 1;
        $user->force_download = 1;
        $user->icon_download = 0;
        $user->attribute_download = 0;
        $user->city_download = 0;
        $user->legend_download = 0;
        $user->last_login_time = 0;
        $user->save();
        return $this->doneResponse();
    }

    public function registerUserFromAdmin(Request $request): \Illuminate\Http\JsonResponse
    {
        if($this->IsUserDuplicate($request["email"]))
        {
            $response["result"] = self::$userExistsCode;
            return response()->json($response);
        }

        $user = new User();
        $user->email = $request["email"];
        $user->password = Hash::make($request["password"]);
        $user->role_id = $request["role_id"];
        $user->force_download = 1;
        $user->icon_download = 0;
        $user->attribute_download = 0;
        $user->city_download = 0;
        $user->legend_download = 0;
        $user->last_login_time = 0;
        $user->save();
        return $this->doneResponse();
    }

    public function editUser(Request $request): \Illuminate\Http\JsonResponse
    {
        $array = array(
            "email" => $request["email"],
            "role_id" => $request["role_id"],
            "force_download" => $request["force_download"],
            "icon_download" => $request["icon_download"],
            "attribute_download" => $request["attribute_download"],
            "city_download" => $request["city_download"],
            "legend_download" => $request["legend_download"]
        );

        $user = DB::table("users")->where("id", $request["id"])->first();

        if($request["email"] != $user->email && $this->IsUserDuplicate($request["email"])) {
            $response["result"] = self::$userExistsCode;
            return response()->json($response);
        }

        if($request["password"] != "old") {
            $array["password"] = Hash::make($request["password"]);
        }

        DB::table("users")
            ->where("id", $request["id"])
            ->update($array);
        return $this->doneResponse();
    }

    public function checkUserParams(Request $request): \Illuminate\Http\JsonResponse
    {
        $user = DB::table("users")->where("id", $request->user()->id)->first();

        try {
            $seconds = $user->last_login_time / 1000;
            $latest = new DateTime("@$seconds");
            $now = new DateTime();
            $diff = $now->diff($latest);

            $months = $diff->m + ($diff->y * 12);

            if($months >= 1) {
                $response["force_download"] = 1;
            } else {
                $response["force_download"] = $user->force_download;
            }
        } catch (\Exception $e) {
            $response["e"]= $e;
        }

        $response["icon_download"] = $user->icon_download;
        $response["attribute_download"] = $user->attribute_download;
        $response["city_download"] = $user->city_download;
        $response["legend_download"] = $user->legend_download;
        $response["user_id"] = $request->user()->id;

        DB::table("users")->where("id", $request->user()->id)
            ->update(array(
                "last_login_time" => $this->getTimeInMillis()
            ));

        return response()->json($response);
    }

    public function disableForceDownload(Request $request): \Illuminate\Http\JsonResponse
    {
        DB::table("users")->where("id", $request->user()->id)->update(array('force_download' => 0));
        DB::table("list")->where("user_id", $request->user()->id)->update(array("force_owner_download" => 0));
        return $this->doneResponse();
    }

    public function disableIconDownload(Request $request): \Illuminate\Http\JsonResponse
    {
        DB::table("users")->where("id", $request->user()->id)->update(array('icon_download' => 0));
        return $this->doneResponse();
    }

    public function disableAttributeDownload(Request $request): \Illuminate\Http\JsonResponse
    {
        DB::table("users")->where("id", $request->user()->id)->update(array('attribute_download' => 0));
        return $this->doneResponse();
    }

    public function disableCityDownload(Request $request): \Illuminate\Http\JsonResponse
    {
        DB::table("users")->where("id", $request->user()->id)->update(array('city_download' => 0));
        return $this->doneResponse();
    }

    public function disableLegendDownload(Request $request): \Illuminate\Http\JsonResponse
    {
        DB::table("users")->where("id", $request->user()->id)->update(array('legend_download' => 0));
        return $this->doneResponse();
    }

    public function synchronizeUser(Request $request): \Illuminate\Http\JsonResponse
    {
        try {
            $boundsID = json_decode($request["boundsID"], true);
            $lastSync = json_decode($request["lastSync"], true);

            $user = DB::table("users")->where("id", $request->user()->id)->first();
            $icon_download = $user->icon_download;
            $attribute_download = $user->attribute_download;
            $city_download = $user->city_download;
            $legend_download = $user->legend_download;
            $force_download = $user->force_download;

            $arrayAdded = array();
            $arrayUpdated = array();
            $arrayDeleted = array();

            if($force_download == 0) {
                $bounds = DB::table("bounds")->where("id", $boundsID)->first();
                $list = DB::table("list")
                    ->select("list.id as id", "list.type as type", "cords.lat as lat", "cords.lng as lng",
                        "list.name as name", "list.icon as icon", "list.updated as updated", "list.user_id as user_id",
                        "list.loadjson_id as loadjson_id", "list.city_id as city_id", "list.deleted as deleted",
                        "list.created as created")
                    ->where("user_id", "!=", $request->user()->id)
                    ->where("updated", ">", $lastSync)
                    ->join('cords', 'list.id', '=', 'cords.idlist')
                    ->where("cords.lat", "<=", $bounds->lat_north)
                    ->where("cords.lat", ">", $bounds->lat_south)
                    ->where("cords.lng", "<=", $bounds->lng_east)
                    ->where("cords.lng", ">", $bounds->lng_west)
                    ->get();

                $forceOwnerDownloadList = DB::table("list")
                    ->select("list.id as id", "list.type as type", "cords.lat as lat", "cords.lng as lng",
                        "list.name as name", "list.icon as icon", "list.updated as updated", "list.user_id as user_id",
                        "list.loadjson_id as loadjson_id", "list.city_id as city_id", "list.deleted as deleted",
                        "list.created as created")
                    ->where("type", "=", "Point")
                    ->where("user_id", "=", $request->user()->id)
                    ->where("force_owner_download", "=", 1)
                    ->where("updated", ">", $lastSync)
                    ->join('cords', 'list.id', '=', 'cords.idlist')
                    ->where("cords.lat", "<=", $bounds->lat_north)
                    ->where("cords.lat", ">", $bounds->lat_south)
                    ->where("cords.lng", "<=", $bounds->lng_east)
                    ->where("cords.lng", ">", $bounds->lng_west)
                    ->get();

                foreach ($list as $row) {
                    if ($row->deleted == 1) {
                        if(!isset($arrayDeleted[$row->id])) {
                            $arrayDeleted[$row->id] = $row->id;
                        }
                    } else if ($row->created == $row->updated) {
                        $this->HandleAddingItemToArray($arrayAdded, $row);
                    } else {
                        $this->HandleAddingItemToArray($arrayUpdated, $row);
                    }
                }

                foreach ($forceOwnerDownloadList as $row) {
                    if ($row->deleted == 1) {
                        if(!isset($arrayDeleted[$row->id])) {
                            $arrayDeleted[$row->id] = $row->id;
                        }
                    } else {
                        $this->HandleAddingItemToArray($arrayUpdated, $row);
                    }

                    DB::table("list")->where("id", $row->id)->update(array("force_owner_download" => 0));
                }
            }

            $response["deleted"] = array_values($arrayDeleted);
            $response["added"] = array_values($arrayAdded);
            $response["updated"] = array_values($arrayUpdated);
            $response["icon_download"] = $icon_download;
            $response["attribute_download"] = $attribute_download;
            $response["city_download"] = $city_download;
            $response["legend_download"] = $legend_download;
            $response["force_download"] = $force_download;
            $response["syncTime"] = $this->getTimeInMillis();

            $compressed = gzdeflate(json_encode($response),  9);
            $final = base64_encode($compressed);
        } catch (\Exception $e) {
            $response["result"] = $e->getMessage();
            throw new HttpException(500, $e->getMessage());
        }

        return response()->json($final);
    }
}
