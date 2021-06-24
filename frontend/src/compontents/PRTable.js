import * as React from 'react';
import { DataGrid } from '@material-ui/data-grid';
import { Button } from 'react-bootstrap';
const columns = [
  // { field: 'id', headerName: 'ID', width: 70 },
  // <Button href={"https://github.com/" + projectName + "/commit/" + linkedCommits[issueNumber][0]}target="_blank" variant="link">{linkedCommits[issueNumber][0]}</Button>
  { field: 'contributor', headerName: 'Developer', type: 'string', align: 'left', width: 200,
  renderCell: (params) => {
    const data = params.data? params.data:params.row;
    // const blah = params.data.state ==='open'?  <ErrorOutlineIcon style={{color:'green'}}/> : <ErrorOutlineIcon color='error'/>
    return <Button href={"https://github.com/" + data.contributor}target="_blank" variant="link">{data.contributor}</Button>

  }


},
  {
    field: 'acceptedOPR',
    headerName: '% OPR',
    description: 'Accepted open pull requests',
    // type: 'number',
    // type: 'number',
    align:'left',
    valueFormatter: ({ value }) => value + '%',
    width: 200,
  },
  {
    field: 'acceptedRPR',
    headerName: '% RPR',
    description: 'Accepted reviewed pull requests',
    // type: 'number',
    align:'left',
    valueFormatter: ({ value }) => value + '%',
    // type: 'number',
    width: 200,
  },
];


export default function DataTable(props) {
  const rows2 = props.data.map((i, index) => {
    return {
      id: index,
      contributor: i.login,
      acceptedOPR: Math.floor(i.openedPRPercent),
      acceptedRPR: Math.floor(i.reviewedPRPercent),
    }
  })
  return (
    <div style={{ height: 500, width: '100%' }}>
      <DataGrid disableSelectionOnClick={true} rows={rows2} columns={columns} pageSize={7} />
    </div>
  );
}
