import React from 'react';
import { MDBTable, MDBTableBody, MDBTableHead } from 'mdbreact';
import IssueDialog from './IssueDialog'
import ErrorOutlineIcon from '@material-ui/icons/ErrorOutline';
import {Button} from 'react-bootstrap';

const IssueTable = (props) => {
  const [projectName] = React.useState(props.projectName);
// console.log("inside Table page ", projectName)
var issues =[];
var issue = {};
var linkedCommits = props.linkedCommits;
props.issues.forEach((i)=>{
  issue = {}
  issue.state = i.state ==='open'? <ErrorOutlineIcon style={{color:'green'}}/> : <ErrorOutlineIcon color='error'/>;
  issue.number = i.number;
  let issueNumber = i.number;
  issue.title = <IssueDialog projectName={projectName} data={i}/>;

  issue.closedbycommit = Object.keys(linkedCommits).includes(issueNumber.toString())?<Button href={"https://github.com/" + projectName + "/commit/" + linkedCommits[issueNumber][0]}target="_blank" variant="link">{linkedCommits[issueNumber][0]}</Button>:""
  issues.push(issue);
})
  
const data = {
  columns: [
    {
      label: 'state',
      field: 'state',
    },
    {
      label: 'number',
      field: 'number',
      sort: 'asc'
    },
    {
      label: 'title',
      field: 'title',
    },
    {
      label: 'closedbycommit',
      field: 'closedbycommit',
    }
    
  ],
  rows: issues
};

return (
    <MDBTable  hover scrollY maxHeight="40vh">
      <MDBTableHead  style={{textAlign:'center'}} columns={data.columns} />
      <MDBTableBody rows={data.rows} />
    </MDBTable>
  );
};

export default IssueTable;