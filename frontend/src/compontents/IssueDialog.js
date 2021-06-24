import React from 'react';
import { Button,  } from 'react-bootstrap';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import SimpleService from '../services/SimpleService'
import IssueCard from './IssueCard'

export default function IssueDialog(props) {
  const [open, setOpen] = React.useState(false);
  const [issue2, setIssue2] = React.useState( {
    body: "hello",
    assignees: [],
    assignee: "",
    created_at: new Date(),
    id: 0,
    labels:[]
  });
  let issue = props.data;
  let projectName = props.projectName;
  const handleClickOpen = () => {
    // console.log(issue);
    SimpleService.getIssueData(issue.id, projectName).then((response) => {
      // console.log("issue Response ", response);
      let rIssue = response.data;
      let issue3={}
      // console.log("this is the create_at",rIssue.created_at)
      issue3.body = rIssue.body;
      issue3.assignees = rIssue.assignees;
      issue3.number = rIssue.number;
      issue3.created_at = new Date(rIssue.createAt);
      issue3.id = rIssue.issueId;
      issue3.title = rIssue.title
      issue3.labels = rIssue.labels
      setOpen(true);
      setIssue2(issue3);

    })
    
  };

  const handleClose = () => {
    setOpen(false);
  };

  return (
    <div>
      <Button style={{textAlign:'left'}} variant='link' onClick={handleClickOpen}>
      {issue.title} <br/>  #{issue.number} 
      </Button>
      <Dialog
        fullWidth={true}
        maxWidth={'md'}
        open={open}
        onClose={handleClose}
        aria-labelledby="alert-dialog-title"
        aria-describedby="alert-dialog-description"
      >
        <DialogTitle  id="alert-dialog-title">{issue2.title} <br/>
        {issue2.labels.map((element,i) => {
          let tcolor = element.color === "FFFFFF"? "black":"white"
          // console.log("element inside issues",element)
        return <Button key={i} disabled style={{color:tcolor, backgroundColor:"#"+element.color}}>{element.name}</Button>
        })}
        </DialogTitle>
        <DialogContent>
          <DialogContentText id="alert-dialog-description">
            <IssueCard data={issue2}/>
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose} color="primary">
            close
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
}
