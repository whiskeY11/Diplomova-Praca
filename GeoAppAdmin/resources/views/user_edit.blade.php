@extends("layouts.app")
@section("content")
    <!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
    <script src="https://kit.fontawesome.com/a076d05399.js"></script>
    <title>GeoApp</title>
    <link rel="stylesheet" type="text/css" href="{{asset('css/app.css?v=').time()}}">
    <link rel="stylesheet" type="text/css" href="{{asset('css/list.css?v=').time()}}">
</head>
<body>
<div class="col-sm-9">
    <div class="well">
        <div class="table-title">
            <h4>Úprava používateľa</h4>
        </div>
        <form class="form-ed" method="post" action="{{action("UserController@EditUser")}}">
            <div class="form-row">
                <div class="form-group col-md-6">
                    <label for="email"><b>Používateľské meno</b></label>
                    <input type="text" id="email" name="email" class="form-control text-ln" value="{{$user->email}}" required autofocus>
                    <label for="password"><b>Nové Heslo</b></label><br/>
                    <input type="password" id="password" name="password" class="form-control text-ln" placeholder="Heslo">
                    <label for="role_id"><b>Rola</b></label><br/>
                    <select class="form-control" id="role_id" name="role_id">
                        @if($user->role_id == 1)
                            <option value="1" selected>Používateľ</option>
                            <option value="2">Administrátor</option>
                        @else
                            <option value="1">Používateľ</option>
                            <option value="2" selected>Administrátor</option>
                        @endif
                    </select>
                </div>
            </div>

            <div class="form-row">
                <div class="form-check-inline">
                    <label class="form-check-label">
                        <input type="checkbox" class="form-check-input" value="" name="force_download" {{$user->force_download == 1 ? "checked" : ""}}>Musí stiahnuť dáta
                    </label>
                </div>
            </div>
            <div class="form-row">
                <div class="form-check-inline">
                    <label class="form-check-label">
                        <input type="checkbox" class="form-check-input" value="" name="icon_download" {{$user->icon_download == 1 ? "checked" : ""}}>Musí stiahnuť ikony
                    </label>
                </div>
            </div>
            <div class="form-row">
                <div class="form-check-inline">
                    <label class="form-check-label">
                        <input type="checkbox" class="form-check-input" value="" name="attribute_download" {{$user->attribute_download == 1 ? "checked" : ""}}>Musí stiahnuť atribúty
                    </label>
                </div>
            </div>
            <div class="form-row">
                <div class="form-check-inline">
                    <label class="form-check-label">
                        <input type="checkbox" class="form-check-input" value="" name="city_download" {{$user->city_download == 1 ? "checked" : ""}}>Musí stiahnuť mestá
                    </label>
                </div>
            </div>
            <div class="form-row">
                <div class="form-check-inline">
                    <label class="form-check-label">
                        <input type="checkbox" class="form-check-input" value="" name="legend_download" {{$user->legend_download == 1 ? "checked" : ""}}>Musí stiahnuť legendy
                    </label>
                </div>
            </div>
            <br/>
            <input type="hidden" name="_token" value="{{csrf_token()}}">
            <input type="hidden" name="id" value="{{$user->id}}">
            <button id="Submit" name="Submit" class="btn btn-outline-primary">Upraviť</button>
            <a class="btn btn-outline-secondary" href="{{action("UserController@ShowUserList")}}" role="button" style="text-decoration: none">Zrušiť</a>
        </form>
    </div>
</div>
</body>
</html>
@endsection("content")
