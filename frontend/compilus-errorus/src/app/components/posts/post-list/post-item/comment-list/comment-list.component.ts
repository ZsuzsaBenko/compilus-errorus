import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { FlComment } from '../../../../../models/FlComment';
import { Post } from '../../../../../models/Post';
import { CommentsService } from '../../../../../services/comments.service';

@Component({
    selector: 'app-comment-list',
    templateUrl: './comment-list.component.html',
    styleUrls: ['./comment-list.component.css']
})
export class CommentListComponent implements OnInit {
    @Input() post: Post;
    @ViewChild('addCommentInput', {static: false}) inputField;
    comments: FlComment[];

    constructor(private commentsService: CommentsService) {
    }

    ngOnInit() {
        this.comments = [];
        this.getComments();
    }

    getComments() {
        const queryString = '?postId=' + this.post.id;
        this.commentsService.getComments(queryString).subscribe(comments => this.comments.push(...comments));
    }

    onUpdated(comment: FlComment) {
        this.commentsService.updateComment(comment).subscribe();
    }

    onDeleted(comment: FlComment) {
        const index = this.comments.indexOf(comment);
        this.comments.splice(index, 1);
        this.commentsService.deleteComment(comment).subscribe();
    }

    addComment() {
        const message = this.inputField.nativeElement.value;
        const newComment = new FlComment();
        newComment.message = message;
        newComment.post = this.post;

        this.inputField.nativeElement.value = '';

        const queryString = '?postId=' + this.post.id;
        this.commentsService.saveComment(newComment).subscribe({
            complete: () => {
                this.commentsService.getComments(queryString).subscribe(comments => this.comments = comments);
            }
        });
    }
}

