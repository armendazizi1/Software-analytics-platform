import * as React from 'react';
import { DataGrid } from '@material-ui/data-grid';

const columns = [
  // { field: 'id', headerName: 'ID', width: 70 },
  { field: 'file', headerName: 'File', align: 'left', width: 350 },
  { field: 'contributor', headerName: 'Top contributor', description: ' the developer who performed the highest number of changes ', type: 'string', width: 260 },
  {
    field: 'changes',
    headerName: '% changes',
    description: 'the percentage of commits impacting the file',
    // type: 'number',
    valueFormatter: ({ value }) => value + '%',
    width: 200,
  },
];

// const rows = [
//   { id:1,file: 'this/is/a/file/file.java', contributor: 'armendazizi1', changes: 100},
//   { id:2,file: 'this/is/a/file/file2.java', contributor: 'bbbb', changes: 32},
//   { id:3,file: 'this/is/a/file/file3.java', contributor: 'ccccc', changes: 14},
//   { id:4,file: 'this/is/a/file/file4.java', contributor: 'abbbbb', changes: 70},

// ];

export default function DataTable(props) {
  const rows2 = props.data.map((i, index) => {
    let fileList = i.file.split('/')

    let filename = fileList.length > 1 ? fileList[fileList.length - 2] + '/' + fileList[fileList.length - 1] : fileList[fileList.length - 1]
    return {
      id: index,
      file: filename,
      contributor: i.developer,
      changes: i.percentage,
    }
  })
  return (
    <div style={{ height: 500, width: '100%' }}>
      <DataGrid disableSelectionOnClick={true} rows={rows2} columns={columns} pageSize={7} />
    </div>
  );
}
