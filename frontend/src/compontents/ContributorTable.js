import * as React from 'react';
import { DataGrid } from '@material-ui/data-grid';

const columns = [
  // { field: 'id', headerName: 'ID', width: 70 },
  { field: 'contributor', headerName: 'Contributor', type: 'string', align: 'left', width: 300, resizable: false },
  { field: 'commits', headerName: 'Absolute expertise', description: 'number of past commits', width: 250 },
];


export default function DataTable(props) {
  const rows2 = props.data.map((i, index) => {
    return {
      id: index,
      contributor: i.name,
      commits: i.expertise
    }
  })
  return (
    <div style={{ height: 500, width: '100%' }}>
      <DataGrid disableSelectionOnClick={true} rows={rows2} columns={columns} pageSize={7} />
    </div>
  );
}
