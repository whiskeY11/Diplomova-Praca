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
                <h4 class="modal-title" id="exampleModalLabel">Pridať Poly</h4>
            </div>
            <form method="post" action="{{action('PolyController@UploadPoly')}}" enctype="multipart/form-data">
                <div class="modal-body">
                    <div class="row">
                        <div class="col-xs-12 col-sm-12 col-md-12">
                            <div class="form-group">
                                <label for="file"><b>Súbor</b></label>
                                <input type="file" accept=".poly" class="form-control-file" name="file" required autofocus><br/>
                                <label for="map_id"><b>Mapy</b></label><br/>
                                <select class="form-control" id="map_id" name="map_id">
                                    @foreach($maps as $row)
                                        <option value="{{$row->id}}">{{$row->name}}</option>
                                    @endforeach
                                </select>
                                <br/>
                                <label for="city"><b>Mesto</b></label><br/>
                                <select class="form-control" id="city" name="city">
                                    @foreach($cities as $row)
                                        <option value="{{$row->id}}">{{$row->name}}</option>
                                    @endforeach
                                </select>
                                <br/> <br/>

                                <div class="form-check-inline">
                                    <label class="form-check-label">
                                        <input type="checkbox" onclick="disableCheckbox()" class="form-check-input" value="" id="create_json" name="create_json">
                                        Vytvoriť JSON s ulicami&nbsp;&nbsp; &nbsp;
                                    </label>
                                    <label class="form-check-label">
                                        <input type="checkbox" disabled class="form-check-input" value="" id="load_json" name="load_json">Použiť dáta (v databáze)
                                    </label>
                                </div>


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
        <div class="table-title">
            <h4>Zoznam</h4>
            <div>
                <a class="btn btn-outline-primary" data-toggle="modal" data-target="#addModal" role="button" style="text-decoration: none">Pridať</a>
            </div>
        </div>
        <table class="table table-hover">
            <thead>
            <tr>
                <th scope="col">Meno</th>
                <th scope="col">JSON s ulicami vytvorený</th>
                <th scope="col">Mesto</th>
                <th scope="col">Mapa</th>
                <th scope="col">Možnosti</th>
            </tr>
            </thead>
            <tbody>
            @foreach ($list as $row)
                <tr>
                    <th scope="row">{{$row->name}}</th>
                    <td>{{$row->json_created == 1 ? "Áno" : "Nie"}}
                    <td>{{$row->city_id == -1 ? "Neurčené" : $row->city_id}}</td>
                    <td>{{$row->map_id}}</td>
                    </td>
                    <td>
                        <a href="{{action('PolyController@ShowEditPoly', ["id" => $row->id])}}"><i class="fas fa-edit" style="color:black"></i></a> |
                        <a href="{{action('PolyController@DeletePoly', ["id" => $row->id])}}" onclick="return confirm('Súbor poly, aj všetky dáta (vrátane ulíc) budú vymazané.');"><i class="fas fa-trash alt" style="color:black"></i></a>
                    </td>
                </tr>
            @endforeach
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
<script>
    function disableCheckbox() {
        var checkBox = document.getElementById("create_json");
        var loadCheckbox = document.getElementById("load_json");

        if (checkBox.checked === true){
            loadCheckbox.disabled = false;
        } else {
            loadCheckbox.disabled = true;
            loadCheckbox.checked = false;
        }
    }
</script>
@endsection
