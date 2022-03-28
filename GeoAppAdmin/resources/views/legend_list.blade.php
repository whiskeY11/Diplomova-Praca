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
    <link rel="stylesheet" type="text/css" href="{{asset('css/legend.css?v=').time()}}">
</head>
<body>

<!-- AddModal -->
<div class="modal fade" id="addModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title" id="exampleModalLabel">Pridať Legendu</h4>
            </div>
            <form method="post" action="{{action('LegendController@InsertLegend')}}" enctype="multipart/form-data">
                <div class="modal-body">
                    <div class="row">
                        <div class="col-xs-12 col-sm-12 col-md-12">
                            <div class="form-group">
                                <input type="text" id="name" name="name" class="form-control text-ln col-md-9" placeholder="Názov" required autofocus><br/>
                                <div class="row col-md-13 text-align-center">
                                    <div class="col">
                                        <label><b>0-10%</b></label>
                                        <input type="color" id="zero" name="zero" value="#ff0000">
                                    </div>
                                    <div class="col">
                                        <label><b>10-20%</b></label>
                                        <input type="color" id="first" name="first" value="#ff0000">
                                    </div>
                                    <div class="col">
                                        <label><b>20-30%</b></label>
                                        <input type="color" id="second" name="second" value="#ff0000">
                                    </div>
                                    <div class="col">
                                        <label><b>30-40%</b></label>
                                        <input type="color" id="third" name="third" value="#ff0000">
                                    </div>
                                    <div class="col">
                                        <label><b>40-50%</b></label>
                                        <input type="color" id="fourth" name="fourth" value="#ff0000">
                                    </div>
                                </div>
                                <br/>
                                <div class="row col-md-13 text-align-center">
                                    <div class="col">
                                        <label><b>50-60%</b></label>
                                        <input type="color" id="fifth" name="fifth" value="#ff0000">
                                    </div>
                                    <div class="col">
                                        <label><b>60-70%</b></label>
                                        <input type="color" id="sixth" name="sixth" value="#ff0000">
                                    </div>
                                    <div class="col">
                                        <label><b>70-80%</b></label>
                                        <input type="color" id="seventh" name="seventh" value="#ff0000">
                                    </div>
                                    <div class="col">
                                        <label><b>80-90%</b></label>
                                        <input type="color" id="eight" name="eight" value="#ff0000">
                                    </div>
                                    <div class="col">
                                        <label><b>90-100%</b></label>
                                        <input type="color" id="ninth" name="ninth" value="#ff0000">
                                    </div>
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
                <th scope="col">Názov</th>
                <th scope="col">0-10%</th>
                <th scope="col">10-20%</th>
                <th scope="col">20-30%</th>
                <th scope="col">30-40%</th>
                <th scope="col">40-50%</th>
                <th scope="col">50-60%</th>
                <th scope="col">60-70%</th>
                <th scope="col">70-80%</th>
                <th scope="col">80-90%</th>
                <th scope="col">90-100%</th>
                <th scope="col">Základná legenda</th>
                <th scope="col">Možnosti</th>
            </tr>
            </thead>
            <tbody>
            @foreach ($list as $row)
                <tr>
                    <th scope="row">{{$row->name}}</th>
                    <td><div class="box" style="background: {{$row->zero}}"></div></td>
                    <td><div class="box" style="background: {{$row->first}}"></div></td>
                    <td><div class="box" style="background: {{$row->second}}"></div></td>
                    <td><div class="box" style="background: {{$row->third}}"></div></td>
                    <td><div class="box" style="background: {{$row->fourth}}"></div></td>
                    <td><div class="box" style="background: {{$row->fifth}}"></div></td>
                    <td><div class="box" style="background: {{$row->sixth}}"></div></td>
                    <td><div class="box" style="background: {{$row->seventh}}"></div></td>
                    <td><div class="box" style="background: {{$row->eight}}"></div></td>
                    <td><div class="box" style="background: {{$row->ninth}}"></div></td>
                    <td>{{$row->default == 1 ? "Áno" : "Nie"}}</td>
                    <td>
                        @if($row->default == 0)
                            <a href="{{action('LegendController@ShowEditLegend', ["id" => $row->id])}}"><i class="fas fa-edit" style="color:black"></i></a> |
                            <a href="{{action('LegendController@DeleteLegend', ["id" => $row->id])}}" onclick="return confirm('Legenda bude vymazaná');"><i class="fas fa-trash alt" style="color:black"></i></a>
                        @else
                            -
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
