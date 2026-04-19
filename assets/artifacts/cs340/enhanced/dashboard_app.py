from __future__ import annotations

import base64
from pathlib import Path
from typing import Any, Dict, List

import pandas as pd
import plotly.express as px
import plotly.graph_objects as go
from dash import Dash, Input, Output, dash_table, dcc, html
import dash_leaflet as dl

from animal_shelter import AnimalShelter
from rescue_queries import build_rescue_query, filter_label, list_filter_options


DEFAULT_CENTER = (30.2672, -97.7431)
TABLE_FALLBACK_COLUMNS = [
    "animal_id",
    "name",
    "animal_type",
    "breed",
    "color",
    "date_of_birth",
    "outcome_type",
    "sex_upon_outcome",
    "age_upon_outcome_in_weeks",
    "location_lat",
    "location_long",
]


def create_dashboard_app() -> Dash:
    shelter = AnimalShelter()
    initial_docs = shelter.read({}, limit=500)
    initial_df = docs_to_df(initial_docs)
    table_columns = (
        list(initial_df.columns) if not initial_df.empty else TABLE_FALLBACK_COLUMNS
    )

    app = Dash(__name__)
    app.layout = build_layout(initial_df, table_columns)

    @app.callback(
        Output("datatable-id", "data"),
        Output("filter-status", "children"),
        Output("record-count", "children"),
        Output("datatable-id", "page_current"),
        Output("datatable-id", "selected_rows"),
        Input("rescue-filter", "value"),
    )
    def update_table(filter_value: str | None):
        query = build_rescue_query(filter_value)
        docs = shelter.read(query, limit=500)
        frame = docs_to_df(docs, table_columns)
        label = filter_label(filter_value)
        status = f"Active Filter: {label}"
        return (
            frame.to_dict("records"),
            status,
            f"Records Returned: {len(frame)}",
            0,
            [],
        )

    @app.callback(
        Output("breed-chart", "figure"),
        Output("outcome-chart", "figure"),
        Input("rescue-filter", "value"),
    )
    def update_analytics(filter_value: str | None):
        query = build_rescue_query(filter_value)
        breed_counts = shelter.breed_counts(query, top_n=10)
        outcome_counts = shelter.outcome_counts(query)
        return build_breed_chart(breed_counts), build_outcome_chart(outcome_counts)

    @app.callback(
        Output("map-layer", "children"),
        Input("datatable-id", "derived_virtual_data"),
        Input("datatable-id", "derived_virtual_selected_rows"),
    )
    def update_map(
        view_data: List[Dict[str, Any]] | None, selected_rows: List[int] | None
    ):
        if not view_data:
            return []

        frame = pd.DataFrame.from_records(view_data)
        if frame.empty:
            return []

        row_index = selected_rows[0] if selected_rows else 0
        row_index = min(row_index, len(frame) - 1)

        lat = pd.to_numeric(frame.loc[row_index].get("location_lat"), errors="coerce")
        lon = pd.to_numeric(frame.loc[row_index].get("location_long"), errors="coerce")
        if pd.isna(lat) or pd.isna(lon):
            lat, lon = DEFAULT_CENTER

        name = str(frame.loc[row_index].get("name", "Unknown"))
        breed = str(frame.loc[row_index].get("breed", "Unknown"))
        outcome_type = str(frame.loc[row_index].get("outcome_type", "Unknown"))

        return [
            dl.Marker(
                position=(float(lat), float(lon)),
                children=[
                    dl.Tooltip(breed),
                    dl.Popup(
                        [
                            html.H4(name),
                            html.P(f"Breed: {breed}"),
                            html.P(f"Outcome: {outcome_type}"),
                        ]
                    ),
                ],
            )
        ]

    return app


def build_layout(initial_df: pd.DataFrame, table_columns: List[str]) -> html.Div:
    return html.Div(
        style={"fontFamily": "Arial, sans-serif", "padding": "16px"},
        children=[
            build_header(),
            html.Hr(),
            html.Div(
                style={
                    "display": "grid",
                    "gridTemplateColumns": "2fr 1fr",
                    "gap": "16px",
                    "marginBottom": "16px",
                },
                children=[
                    html.Div(
                        children=[
                            html.H3("Rescue Candidate Filters"),
                            dcc.RadioItems(
                                id="rescue-filter",
                                options=list_filter_options(),
                                value="RESET",
                                labelStyle={
                                    "display": "inline-block",
                                    "marginRight": "16px",
                                },
                            ),
                            html.Div(
                                id="filter-status",
                                style={"fontStyle": "italic", "marginTop": "8px"},
                            ),
                        ]
                    ),
                    html.Div(
                        style={
                            "backgroundColor": "#f7f7f7",
                            "padding": "12px",
                            "borderRadius": "8px",
                        },
                        children=[
                            html.H4("Dashboard Summary"),
                            html.Div(
                                id="record-count",
                                children=f"Records Returned: {len(initial_df)}",
                            ),
                            html.Div(
                                "Analytics are now generated with MongoDB aggregation instead of only client-side pandas counts."
                            ),
                        ],
                    ),
                ],
            ),
            dash_table.DataTable(
                id="datatable-id",
                columns=[
                    {"name": col, "id": col, "selectable": True}
                    for col in table_columns
                ],
                data=initial_df.to_dict("records"),
                page_size=12,
                page_action="native",
                sort_action="native",
                filter_action="native",
                row_selectable="single",
                selected_rows=[],
                fixed_rows={"headers": True},
                style_table={
                    "overflowX": "auto",
                    "overflowY": "auto",
                    "height": "420px",
                },
                style_cell={
                    "textAlign": "left",
                    "padding": "6px",
                    "minWidth": "110px",
                    "whiteSpace": "normal",
                },
                style_header={"fontWeight": "bold", "backgroundColor": "#e9eef6"},
            ),
            html.Br(),
            html.Div(
                style={
                    "display": "grid",
                    "gridTemplateColumns": "1fr 1fr",
                    "gap": "16px",
                },
                children=[
                    dcc.Graph(id="breed-chart", figure=empty_figure("Top Breeds")),
                    dcc.Graph(
                        id="outcome-chart", figure=empty_figure("Outcome Distribution")
                    ),
                ],
            ),
            html.Br(),
            html.Div(
                children=[
                    html.H3("Selected Animal Location"),
                    dl.Map(
                        center=DEFAULT_CENTER,
                        zoom=10,
                        style={"width": "100%", "height": "520px"},
                        children=[dl.TileLayer(), dl.LayerGroup(id="map-layer")],
                    ),
                ]
            ),
        ],
    )


def build_header() -> html.Div:
    logo_src = load_logo_data_uri(Path(__file__).with_name("Grazioso Salvare Logo.png"))
    return html.Div(
        style={"textAlign": "center"},
        children=[
            html.A(
                href="https://www.snhu.edu",
                target="_blank",
                children=html.Img(
                    src=logo_src, style={"height": "90px", "marginBottom": "10px"}
                ),
            ),
            html.H1("Grazioso Salvare Rescue Dashboard"),
            html.P(
                "Enhanced CS 340 dashboard with secure configuration, validated queries, and MongoDB aggregation analytics."
            ),
        ],
    )


def load_logo_data_uri(path: Path) -> str:
    if not path.exists():
        return ""
    encoded = base64.b64encode(path.read_bytes()).decode("utf-8")
    return f"data:image/png;base64,{encoded}"


def docs_to_df(
    docs: List[Dict[str, Any]], columns: List[str] | None = None
) -> pd.DataFrame:
    frame = pd.DataFrame.from_records(docs)
    if frame.empty:
        return pd.DataFrame(columns=columns or TABLE_FALLBACK_COLUMNS)

    if "_id" in frame.columns:
        frame = frame.drop(columns=["_id"])

    for numeric_column in [
        "location_lat",
        "location_long",
        "age_upon_outcome_in_weeks",
    ]:
        if numeric_column in frame.columns:
            frame[numeric_column] = pd.to_numeric(
                frame[numeric_column], errors="coerce"
            )

    ordered_columns = columns or list(frame.columns)
    return frame.reindex(columns=ordered_columns)


def build_breed_chart(records: List[Dict[str, Any]]) -> go.Figure:
    if not records:
        return empty_figure("Top Breeds (No Data)")
    frame = pd.DataFrame(records)
    return px.bar(
        frame,
        x="breed",
        y="count",
        title="Top Breeds",
        labels={"count": "Animals", "breed": "Breed"},
    )


def build_outcome_chart(records: List[Dict[str, Any]]) -> go.Figure:
    if not records:
        return empty_figure("Outcome Distribution (No Data)")
    frame = pd.DataFrame(records)
    return px.pie(
        frame, names="outcome_type", values="count", title="Outcome Distribution"
    )


def empty_figure(title: str) -> go.Figure:
    fig = go.Figure()
    fig.update_layout(title=title)
    return fig


if __name__ == "__main__":
    app = create_dashboard_app()
    app.run(debug=False)
