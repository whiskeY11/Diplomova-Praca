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

<!-- AddModal -->
<div class="modal fade" id="addModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title" id="exampleModalLabel">Pridať Používateľa</h4>
            </div>
            <form method="post" action="{{action('UserController@AddUser')}}" enctype="multipart/form-data">
                <div class="modal-body">
                    <div class="row">
                        <div class="col-xs-12 col-sm-12 col-md-12">
                            <div class="form-group">
                                <label for="email"><b>Používateľské meno</b></label>
                                <input type="text" id="email" name="email" class="form-control text-ln" placeholder="asined11" required autofocus>
                                <label for="password"><b>Heslo</b></label><br/>
                                <input type="password" id="password" name="password" class="form-control text-ln" placeholder="*********" required>
                                <label for="role_id"><b>Rola</b></label><br/>
                                <select class="form-control" id="role_id" name="role_id">
                                    <option value="1" selected>Používateľ</option>
                                    <option value="2">Administrátor</option>
                                </select><br/>
                                <input type="hidden" name="_token" value="{{csrf_token()}}">
                            </div>
                        </div>
                    </div>

                </div>
                <div class="modal-footer">
                    <input class="btn btn-primary" type="submit" name="submit" value="Pridať">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Zavrieť</button>
                </div>
            </form>
        </div>
    </div>
</div>

<div class="col-sm-9">
    <div class="well">
        @if(session()->has("userexists"))
            <div class="alert alert-danger">
                <div style="text-align: center">
                    Používateľ s menom {{session("userexists")}} už existuje! <br/><br/>
                    <a class="btn btn-outline-warning btn-warning" href="{{action('UserController@DumpUserExists')}}" role="button" style="text-decoration: none">Zavrieť</a>
                </div>
            </div>
        @endif
        <div class="table-title">
            <h4>Zoznam</h4>
            <div>
                <a class="btn btn-outline-primary" data-toggle="modal" data-target="#addModal" role="button" style="text-decoration: none">Pridať</a>
            </div>
        </div>
        <table class="table table-hover">
            <thead>
            <tr>
                <th scope="col">Používateľské meno</th>
                <th scope="col">Rola</th>
                <th scope="col">Naposledy prihlásený</th>
                <th scope="col">Musí stiahnuť dáta</th>
                <th scope="col">Musí stiahnuť ikony</th>
                <th scope="col">Musí stiahnuť atribúty</th>
                <th scope="col">Musí stiahnuť mestá</th>
                <th scope="col">Musí stiahnuť legendy</th>
                <th scope="col">Možnosti</th>
            </tr>
            </thead>
            <tbody>
            @foreach ($list as $row)
                <tr>
                    <th scope="row">{{$row->email}}</th>
                    <td>
                        @if($row->role_id == 2)
                            Administrátor
                        @else
                            Používateľ
                        @endif
                    </td>
                    <td>{{$row->last_login_time == 0 || $row->role_id == 2 ? "Nezaznamenané" : $row->last_login_time}}</td>
                    <td>{{$row->force_download == 1 ? "Áno" : "Nie"}}</td>
                    <td>{{$row->icon_download == 1 ? "Áno" : "Nie"}}</td>
                    <td>{{$row->attribute_download == 1 ? "Áno" : "Nie"}}</td>
                    <td>{{$row->city_download == 1 ? "Áno" : "Nie"}}</td>
                    <td>{{$row->legend_download == 1 ? "Áno" : "Nie"}}</td>
                    <td>
                        <a href="{{action('UserController@ShowEditUser', ["id" => $row->id])}}"><i class="fas fa-edit" style="color:black"></i></a> |
                        <a href="{{action('UserController@DeleteUser', ["id" => $row->id])}}" onclick="return confirm('Používateľ aj jeho pridané body budú vymazané.');"><i class="fas fa-trash alt" style="color:black"></i></a>
                    </td>
                </tr>
            @endforeach
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
@endsection
