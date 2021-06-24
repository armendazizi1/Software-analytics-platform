import * as React from 'react';
import { DataGrid } from '@material-ui/data-grid';
import { Button } from 'react-bootstrap';
import ErrorOutlineIcon from '@material-ui/icons/ErrorOutline';
import IssueDialog from './IssueDialog'

const columns2=[
  { field: 'id', headerName: 'ID', width: 70, hide:true  },
  {
    field: 'state2',
    headerName: 'State',
    width: 120,
    renderCell: (params) => {
      // console.log("this is params", params)
      const data = params.data? params.data:params.row;
      const blah = data.state ==='open'?  <ErrorOutlineIcon style={{color:'green', margin:'0%',padding:'0%'}}/> : <ErrorOutlineIcon color='error'/>
      return (
        <div>
          {blah}

          {data.closedbycommit}
        </div>
      )
    }
    ,
  },

  { field: 'state', headerName: 'State2', width: 80, hide:true  },
  {
    field: 'title',
    headerName: 'Title',
    width: 700,
    renderCell: (params) => {
      const data = params.data? params.data:params.row;
      // const blah = params.data.state ==='open'?  <ErrorOutlineIcon style={{color:'green'}}/> : <ErrorOutlineIcon color='error'/>
      return data.title
    }
    ,
  },

]


const DataTable = (props)=>  {
const [projectName] = React.useState(props.projectName);
// console.log("inside Table page ", props.issues)
var issues =[];
var issue = {};
var linkedCommits = props.linkedCommits;
props.issues.forEach((i,index)=>{
  // console.log("tjis is index",index)
  issue = {}
  issue.id = index
  issue.state = i.state;
  issue.state2  = ""
  // issue.state = i.state ==='open'? <ErrorOutlineIcon style={{color:'green'}}/> : <ErrorOutlineIcon color='error'/>;
  issue.number = i.number;
  let issueNumber = i.number;
  issue.title = <IssueDialog projectName={projectName} data={i}/>;
  // issue.title = i.title;

  issue.closedbycommit = Object.keys(linkedCommits).includes(issueNumber.toString())?<Button href={"https://github.com/" + projectName + "/commit/" + linkedCommits[issueNumber][0]}target="_blank" variant="link">{linkedCommits[issueNumber][0].slice(0,7)}</Button>:""
  issues.push(issue);
})

  return (
    <div style={{ height: 500, width: '100%' }}>
      <DataGrid disableSelectionOnClick={true} rows={issues} columns={columns2} pageSize={7} />
    </div>
  );
}


export default DataTable
