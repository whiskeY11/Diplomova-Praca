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
                <h4 class="modal-title" id="exampleModalLabel">Pridať CSV</h4>
            </div>
            <form method="post" action="{{action('CsvController@UploadCSV')}}" enctype="multipart/form-data">
                <div class="modal-body">
                    <div class="row">
                        <div class="col-xs-12 col-sm-12 col-md-12">
                            <div class="form-group">
                                <label for="file"><b>CSV Súbor</b></label>
                                <input type="file" accept=".csv" class="form-control-file" name="file" required autofocus><br/>
                                <label for="city"><b>Mesto</b></label><br/>
                                <div class="row">
                                    <div class="col-6">
                                        <select class="form-control" id="city" name="city">
                                            @foreach($cities as $row)
                                                <option value="{{$row->id}}">{{$row->name}}</option>
                                            @endforeach
                                        </select>
                                    </div>
                                </div><br/>
                                <label for="legend"><b>Legenda</b></label><br/>
                                <div class="row">
                                    <div class="col-6">
                                        <select class="form-control" id="legend" name="legend">
                                            @foreach($legends as $row)
                                                <option value="{{$row->id}}">{{$row->name}}</option>
                                            @endforeach
                                        </select>
                                    </div>
                                </div> <br/>
                                <div class="form-check-inline">
                                    <label class="form-check-label">
                                        <input type="checkbox" class="form-check-input" value="" name="load">Použiť atribúty (v databáze)
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

<!-- NotFoundModal -->
<div class="modal fade" id="notFoundModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title" id="exampleModalLabel">Nenájdené entity
                    @if(session()->has("notfound"))
                        ({{session("notfound")[0]}})
                    @endif
                </h4>
            </div>
            <div class="modal-body">
                <div class="row">
                    <div class="col-xs-12 col-sm-12 col-md-12">
                        @if(session()->has("notfound"))
                            @for($i = 1; $i < count(session("notfound")); $i++)
                                {{session("notfound")[$i]}}<br/>
                            @endfor
                        @endif
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Zavrieť</button>
            </div>
        </div>
    </div>
</div>

<div class="col-sm-9">
    <div class="well">
        @if(session()->has("notfound"))
            <div class="alert alert-danger">
                <div style="text-align: center">
                    Niektoré entity pre atribútu {{session("notfound")[0]}} sa v databáze nenachádzajú <br/> Počet: {{count(session("notfound"))-1}} <br/><br/>
                    <a class="btn btn-outline-warning btn-warning" data-toggle="modal" data-target="#notFoundModal" role="button" style="text-decoration: none">Ukázať</a>
                    <a class="btn btn-outline-warning btn-warning" href="{{action('CsvController@DumpNotFound')}}" role="button" style="text-decoration: none">Zavrieť</a>
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
                <th scope="col">Názov</th>
                <th scope="col">Mesto</th>
                <th scope="col">Legenda</th>
                <th scope="col">Atribúty použité v databáze</th>
                <th scope="col">Možnosti</th>
            </tr>
            </thead>
            <tbody>
            @foreach ($list as $row)
                <tr>
                    <th scope="row">{{$row->name}}</th>
                    <td>{{$row->city_id == -1 ? "Neurčené" : $row->city_id}}</td>
                    <td>{{$row->legend}}</td>
                    <td>{{$row->loaded == 1 ? "Áno" : "Nie"}}</td>
                    <td>
                        <a href="{{action('CsvController@ShowEditCSV', ["id" => $row->id])}}"><i class="fas fa-edit" style="color:black"></i></a> |
                        <a href="{{action('CsvController@DeleteCSV', ["id" => $row->id])}}" onclick="return confirm('CSV súbor, aj všetky pridané atribúty z tohto súboru, budú vymazané.');"><i class="fas fa-trash alt" style="color:black"></i></a>
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
