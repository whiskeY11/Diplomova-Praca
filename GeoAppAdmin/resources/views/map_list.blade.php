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
                <h4 class="modal-title" id="exampleModalLabel">Pridať Mapy</h4>
            </div>
            <form method="post" action="{{action('MapController@UploadMap')}}" enctype="multipart/form-data">
                <div class="modal-body">
                    <div class="row">
                        <div class="col-xs-12 col-sm-12 col-md-12">
                            <div class="form-group">
                                <label for="file"><b>Súbor</b></label>
                                <input type="file" accept=".pbf" class="form-control-file" name="file" required autofocus><br/>
                                <br/> <br/>

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
                <th scope="col">Typ</th>
                <th scope="col">Základná mapa</th>
                <th scope="col">Možnosti</th>
            </tr>
            </thead>
            <tbody>
            @foreach ($list as $row)
                <tr>
                    <th scope="row">{{$row->name}}</th>
                    <td>{{$row->type}}</td>
                    <td>{{$row->default == 1 ? "Áno" : "Nie"}}</td>
                    <td>
                        @if($row->default == 0)
                            <a href="{{action('MapController@ShowUpdateMap', ["id" => $row->id])}}"><i class="fas fa-wrench" style="color:black"></i></a> |
                            <a href="{{action('MapController@ShowEditMap', ["id" => $row->id])}}"><i class="fas fa-edit" style="color:black"></i></a> |
                            <a href="{{action('MapController@DeleteMap', ["id" => $row->id])}}" onclick="return confirm('Mapy budú vymazané.');"><i class="fas fa-trash alt" style="color:black"></i></a>
                        @else
                            <a href="{{action('MapController@ShowUpdateMap', ["id" => $row->id])}}"><i class="fas fa-wrench" style="color:black"></i></a>
                        @endif
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
